package com.app.drivn.backend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "web-security")
class WebSecurityProperties {
    var unauthorizedPaths: Array<String> = emptyArray()
    var anonymousPaths: Array<String> = emptyArray()
    var roleAccessRestrictionPaths: List<RoleAccessRestrictionPath>? = null

    /**
     * Role hierarchy list.
     *
     *
     * For example:
     * <blockquote><pre>
     * web-security:
     * role-hierarchy: ROLE_ADMIN > ROLE_STAFF, ROLE_STAFF > ROLE_USER
    </pre></blockquote> *
     *
     * @see org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
     */
    var roleHierarchy: Array<String> = emptyArray()

    /**
     * The type Role access restriction path.
     */
    class RoleAccessRestrictionPath {
        var methods: Array<String> = emptyArray()
        var roles: Array<String> = emptyArray()
        var paths: Array<String> = emptyArray()
    }
}