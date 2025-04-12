package com.bnm.individuals_api.configuration;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class KeycloakJwtAuthenticationConverter implements
    Converter<Jwt, Mono<AbstractAuthenticationToken>> {

  @Override
  public Mono<AbstractAuthenticationToken> convert(final Jwt jwt) {
    return Mono.fromCallable(() -> {
      final KeycloakUserDetails userDetails = new KeycloakUserDetails(jwt);
      return new JwtAuthenticationToken(jwt, userDetails.getAuthorities());
    });
  }
} 