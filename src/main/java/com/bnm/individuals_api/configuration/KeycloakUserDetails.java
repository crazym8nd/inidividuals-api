package com.bnm.individuals_api.configuration;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

@Getter
public class KeycloakUserDetails implements UserDetails {

  private final String id;
  private final String username;
  private final String email;
  private final Instant createdAt;
  private final Collection<? extends GrantedAuthority> authorities;

  public KeycloakUserDetails(final Jwt jwt) {
    this.id = jwt.getSubject();
    this.username = jwt.getClaimAsString("preferred_username");
    this.email = jwt.getClaimAsString("email");
    this.createdAt = Instant.ofEpochMilli(jwt.getClaim("createdTimestamp"));

    final Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
    final Collection<String> roles = (Collection<String>) realmAccess.get("roles");
    this.authorities = roles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toList());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
} 