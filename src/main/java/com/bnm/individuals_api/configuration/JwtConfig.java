package com.bnm.individuals_api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@Configuration
public class JwtConfig {

  @Bean
  public ReactiveJwtDecoder jwtDecoder() {
    return new NimbusReactiveJwtDecoder(
        "http://localhost:9005/realms/appauth/protocol/openid-connect/certs");
  }
} 