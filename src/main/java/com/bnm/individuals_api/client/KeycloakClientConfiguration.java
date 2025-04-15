package com.bnm.individuals_api.client;

import com.bnm.individuals_api.configuration.KeycloakIntegrationExternalServiceProperties;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KeycloakIntegrationExternalServiceProperties.class)
public class KeycloakClientConfiguration {

  private final KeycloakIntegrationExternalServiceProperties properties;

  public KeycloakClientConfiguration(final KeycloakIntegrationExternalServiceProperties properties) {
    this.properties = properties;
  }

  @Bean
  public Keycloak keycloak() {
    return KeycloakBuilder.builder()
        .serverUrl(properties.authUrl())
        .realm(properties.realm())
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(properties.clientId())
        .clientSecret(properties.clientSecret())
        .build();
  }
}
