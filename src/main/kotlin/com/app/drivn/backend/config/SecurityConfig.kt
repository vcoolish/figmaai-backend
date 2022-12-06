package com.app.drivn.backend.config

import com.app.drivn.backend.common.util.logger
import com.app.drivn.backend.config.properties.CorsProperties
import com.app.drivn.backend.config.properties.WebSecurityProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.access.hierarchicalroles.NullRoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.firewall.HttpFirewall
import org.springframework.security.web.firewall.StrictHttpFirewall
import org.springframework.stereotype.Service
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import java.security.SecureRandom
import java.util.HashSet
import java.util.function.Predicate


@Service
@EnableConfigurationProperties(
  WebSecurityProperties::class,
  CorsProperties::class
)
class SecurityConfig(
  protected val webSecurityProps: WebSecurityProperties,
  protected val corsProperties: CorsProperties,
  private val properties: ServerProperties,
  private val sigFilter: SigFilter,
) {

  @Bean
  fun passwordEncoder(): PasswordEncoder {
    return BCryptPasswordEncoder()
  }

  @Bean
  fun secureRandom(): SecureRandom {
    return SecureRandom()
  }

  @Bean
  fun roleHierarchy(): RoleHierarchy {
    if (webSecurityProps.roleHierarchy.isEmpty()) {
      return NullRoleHierarchy()
    }
    val roleHierarchy = RoleHierarchyImpl()
    roleHierarchy.setHierarchy(java.lang.String.join("\n", *webSecurityProps.roleHierarchy))
    return roleHierarchy
  }

  @Bean
  fun httpFirewall(): HttpFirewall {
    val firewall = StrictHttpFirewall()
    firewall.setUnsafeAllowAnyHttpMethod(true)

    return firewall
  }

  @Bean
  @ConditionalOnProperty("server.cors.enabled", matchIfMissing = true)
  fun corsFilter(): CorsFilter {
    val filter = CorsFilter(corsConfigurationSource())
    filter.setCorsProcessor(ExtendedCorsProcessor())

    return filter
  }

  @Throws(Exception::class)
  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    val logger = logger()
    logger.info("start filter")
    http
      .logout().disable()
      .httpBasic().disable()
      .csrf().disable()
      .exceptionHandling()
//          .authenticationEntryPoint(SecurityAuthenticationEntryPoint(objectMapper))
      .accessDeniedHandler(SecurityAccessDeniedHandler())
      .and()
      .sessionManagement { c: SessionManagementConfigurer<HttpSecurity?> ->
        c.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      }
      .addFilterAt(sigFilter, UsernamePasswordAuthenticationFilter::class.java)

    val urlRegistry = http.authorizeRequests()

    // set error path open for all by default
    urlRegistry.antMatchers(properties.error.path).permitAll()

    applyUnauthorizedPaths(urlRegistry)
    applyAnonymousPaths(urlRegistry)
    applyRoleRestrictions(urlRegistry)

    logger.info("cors ${corsProperties.isEnabled}")
    if (corsProperties.isEnabled) {
      http.cors().configurationSource(null)
    } else {
      http.cors().disable()
    }

    return http.build()
  }

  protected fun applyUnauthorizedPaths(
    urlRegistry: ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
  ) {
    if (webSecurityProps.unauthorizedPaths.isEmpty()) {
      return
    }
    urlRegistry.antMatchers(*webSecurityProps.unauthorizedPaths).permitAll()
  }

  protected fun applyAnonymousPaths(
    urlRegistry: ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
  ) {
    if (webSecurityProps.anonymousPaths.isEmpty()) {
      return
    }
    urlRegistry.antMatchers(*webSecurityProps.anonymousPaths).anonymous()
  }

  protected fun applyRoleRestrictions(
    urlRegistry: ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry
  ) {
    val paths = webSecurityProps.roleAccessRestrictionPaths
    if (paths.isNullOrEmpty()) {
      return
    }
    for (roleRestriction in paths) {
      val methods = roleRestriction.methods
      if (methods.isEmpty()) {
        val matchers = urlRegistry.antMatchers(*roleRestriction.paths)
        if (roleRestriction.roles.isNotEmpty()) {
          matchers.hasAnyAuthority(*roleRestriction.roles)
        } else {
          matchers.authenticated()
        }
        continue
      }
      for (method in methods) {
        val matchers = urlRegistry.antMatchers(method, *roleRestriction.paths)
        if (roleRestriction.roles.isNotEmpty()) {
          matchers.hasAnyAuthority(*roleRestriction.roles)
        } else {
          matchers.authenticated()
        }
      }
    }
  }

  protected fun corsConfigurationSource(): CorsConfigurationSource {
    val source = UrlBasedCorsConfigurationSource()
    if (corsProperties.configurations != null) {
      for (configuration in corsProperties.configurations!!) {
        source.registerCorsConfiguration(configuration.path!!, configuration.configuration!!)
      }
    }
    return source
  }
}