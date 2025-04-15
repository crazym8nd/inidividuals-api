package com.bnm.individuals_api.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class ReactiveSecurityConfig {

  private final KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;
  private final ReactiveJwtDecoder jwtDecoder;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
    return http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(exchange -> exchange
            .pathMatchers(
                "/v1/auth/registration",
                "/v1/auth/login",
                "/v1/auth/refresh-token",
                "/actuator/**",
                "/v3/api-docs",
                "/v3/api-docs.yaml"
            ).permitAll()
            .anyExchange().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .jwtDecoder(jwtDecoder)
                .jwtAuthenticationConverter(keycloakJwtAuthenticationConverter)
            )
        )
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        .build();
  }
} 