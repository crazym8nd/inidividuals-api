package com.bnm.individuals_api;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KeycloakTestContainer {

  static final KeycloakContainer keycloak;
  private static final String CLIENT_ID = "app-auth-client-id";
  private static final String CLIENT_SECRET = "FuXDordwvRlgaCasQ1xsqi6s9pkAuQPI";

  static {
    keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:24.0.4")
        .withRealmImportFile("realm-export-test.json")
        .withEnv("DB_VENDOR", "h2")
        .withEnv("DB_URL", "jdbc:h2:mem:testdb")
        .withEnv("DB_USER", "sa")
        .withEnv("DB_PASSWORD", "");
    keycloak.start();
  }


  @DynamicPropertySource
  static void registerResourceServerIssuerProperty(final DynamicPropertyRegistry registry) {
    registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
        () -> keycloak.getAuthServerUrl() + "/realms/appauth");
    registry.add("keycloak-service.client.base-admin-url",
        () -> keycloak.getAuthServerUrl() + "/admin/realms/appauth");
    registry.add("keycloak.clientId", () -> CLIENT_ID);
    registry.add("keycloak.clientSecret", () -> CLIENT_SECRET);
    registry.add("keycloak.urls.auth", () -> keycloak.getAuthServerUrl());
  }

  @Bean
  @Primary
  public Keycloak keycloak() {
    return KeycloakBuilder.builder()
        .serverUrl(keycloak.getAuthServerUrl())
        .realm("appauth")
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(CLIENT_ID)
        .clientSecret(CLIENT_SECRET)
        .build();
  }
}
