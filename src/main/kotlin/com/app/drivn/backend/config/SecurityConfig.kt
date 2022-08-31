package com.app.drivn.backend.config

import com.app.drivn.backend.config.properties.AppProperties
import com.app.drivn.backend.config.properties.CorsProperties
import com.app.drivn.backend.config.properties.WebSecurityProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.access.hierarchicalroles.NullRoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Service
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.security.SecureRandom


@Service
@EnableConfigurationProperties(
  WebSecurityProperties::class,
  CorsProperties::class,
  AppProperties::class
)
class SecurityConfig(
  protected val webSecurityProps: WebSecurityProperties,
  protected val corsProperties: CorsProperties,
  private val properties: ServerProperties,
  private val appProperties: AppProperties,
) : WebSecurityConfigurerAdapter() {

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

  @Throws(Exception::class)
  override fun configure(http: HttpSecurity) {
    http
      .logout().disable()
      .httpBasic().disable()
      .csrf().disable()
      //            .exceptionHandling(c -> c
      //                    .authenticationEntryPoint(new SecurityAuthenticationEntryPoint(objectMapper))
      //                    .accessDeniedHandler(new SecurityAccessDeniedHandler(objectMapper)))
      .sessionManagement { c: SessionManagementConfigurer<HttpSecurity?> ->
        c.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      }
      .addFilterAt(SigFilter(appProperties), UsernamePasswordAuthenticationFilter::class.java)

    val urlRegistry = http.authorizeRequests()

    // set error path open for all by default
    urlRegistry.antMatchers(properties.error.path).permitAll()
    applyUnauthorizedPaths(urlRegistry)
    applyAnonymousPaths(urlRegistry)
    applyRoleRestrictions(urlRegistry)
    if (corsProperties.isEnabled) {
      http.cors().configurationSource(corsConfigurationSource())
    } else {
      http.cors().disable()
    }
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
        urlRegistry
          .antMatchers(*roleRestriction.paths)
          .hasAnyAuthority(*roleRestriction.roles)
        continue
      }
      for (method in methods) {
        urlRegistry
          .antMatchers(HttpMethod.resolve(method), *roleRestriction.paths)
          .hasAnyAuthority(*roleRestriction.roles)
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