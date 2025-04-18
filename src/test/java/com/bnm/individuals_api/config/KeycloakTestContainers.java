package com.bnm.individuals_api.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Slf4j
public abstract class KeycloakTestContainers {

  private static final KeycloakContainer keycloak;

  static {
    keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.2.0")
        .withRealmImportFile("realm-export-test.json")
        .withEnv("DB_VENDOR", "h2")
        .withEnv("DB_URL", "jdbc:h2:mem:testdb")
        .withEnv("DB_USER", "sa")
        .withEnv("DB_PASSWORD", "");
    keycloak.start();
  }

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("keycloak.authUrl", keycloak::getAuthServerUrl);
  }
} 