package com.app.drivn.backend.config

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
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer.*
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.firewall.HttpFirewall
import org.springframework.security.web.firewall.StrictHttpFirewall
import org.springframework.stereotype.Service
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.security.SecureRandom
import java.util.function.Predicate
import java.util.regex.Pattern

@Service
@EnableConfigurationProperties(WebSecurityProperties::class, CorsProperties::class)
class SecurityConfig(
    protected val webSecurityProps: WebSecurityProperties,
    protected val corsProperties: CorsProperties,
    private val properties: ServerProperties
) : WebSecurityConfigurerAdapter() {
    @Bean
    fun httpFirewall(): HttpFirewall {
        val firewall = StrictHttpFirewall()
        firewall.setAllowedHeaderValues(ASSIGNED_AND_NOT_ISO_CONTROL_PREDICATE)
        return firewall
    }

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

    override fun configure(web: WebSecurity) {
        web
            .ignoring()
            .antMatchers(
                "/v2/api-docs",
                "/v3/api-docs",
                "/configuration/",
                "/swagger*/**",
                "/webjars/**"
            )
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
        //            .addFilterAt(sessionAccessTokenFilter, UsernamePasswordAuthenticationFilter.class)
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
        if (paths == null || paths.isEmpty()) {
            return
        }
        for (roleRestriction in paths) {
            val methods = roleRestriction.methods
            if (methods.size == 0) {
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

    companion object {
        @Suppress("RegExpSimplifiable")
        private val ASSIGNED_AND_NOT_ISO_CONTROL_PATTERN = Pattern.compile("[\\p{IsAssigned}&&[^\\p{IsControl}]]*")
        private val ASSIGNED_AND_NOT_ISO_CONTROL_PREDICATE =
            Predicate { s: String? -> ASSIGNED_AND_NOT_ISO_CONTROL_PATTERN.matcher(s).matches() }
    }
}