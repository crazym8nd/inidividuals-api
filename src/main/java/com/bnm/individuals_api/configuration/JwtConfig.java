package com.bnm.individuals_api.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@Configuration
public class JwtConfig {

  @Value("${jwt.jwk-set-uri}")
  private String jwkSetUri;

  @Bean
  public ReactiveJwtDecoder jwtDecoder() {
    return new NimbusReactiveJwtDecoder(jwkSetUri);
  }
} 