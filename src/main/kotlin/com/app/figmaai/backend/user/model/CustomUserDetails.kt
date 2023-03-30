package com.app.figmaai.backend.user.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(val user: User) : UserDetails {

  override fun getUsername() = user.userUuid
  override fun getPassword() = user.password
  override fun isEnabled() = user.enabled
  override fun getAuthorities(): MutableCollection<out GrantedAuthority> = arrayListOf(GrantedAuthority { "" })
  override fun isCredentialsNonExpired() = true
  override fun isAccountNonExpired() = true
  override fun isAccountNonLocked() = true
}