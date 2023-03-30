package com.app.figmaai.backend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpMethod

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
    var methods: Array<HttpMethod> = emptyArray()
    var roles: Array<String> = emptyArray()
    var paths: Array<String> = emptyArray()
  }
}