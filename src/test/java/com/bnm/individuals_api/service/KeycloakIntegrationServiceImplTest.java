package com.bnm.individuals_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bnm.individuals_api.config.KeycloakTestContainers;
import com.bnm.individuals_api.configuration.KeycloakIntegrationExternalServiceProperties;
import com.bnm.individuals_api.exception.EmailAlreadyRegisteredException;
import com.bnm.individuals_api.exception.UnauthorizedCredentialsException;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.UserRegistration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
class KeycloakIntegrationServiceImplTest extends KeycloakTestContainers {

  private KeycloakIntegrationServiceImpl keycloakIntegrationService;
  private Keycloak adminClient;

  @Autowired
  private KeycloakIntegrationExternalServiceProperties properties;

  @Autowired
  private WebClient webClient;

  @BeforeEach
  void setUp() {
    keycloakIntegrationService = new KeycloakIntegrationServiceImpl(properties, webClient);
    adminClient = KeycloakBuilder.builder()
        .serverUrl(properties.authUrl())
        .realm(properties.realm())
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(properties.clientId())
        .clientSecret(properties.clientSecret())
        .build();
  }

  @AfterEach
  void tearDown() {
    adminClient.realm(properties.realm())
        .users()
        .list()
        .forEach(user ->
            adminClient.realm(properties.realm())
                .users()
                .delete(user.getId())
        );
  }

  @Test
  void shouldSuccessfullyRegisterNewUser() {
    // given
    final UserRegistration userRegistrationRequest = new UserRegistration(
        "test@example.com",
        "Password123!",
        "Password123!"
    );

    // when & then
    StepVerifier.create(keycloakIntegrationService.registerUser(userRegistrationRequest))
        .assertNext(authData -> {
          assertNotNull(authData.accessToken());
          assertNotNull(authData.refreshToken());
          assertTrue(authData.expiresIn() > 0);
          assertEquals("Bearer", authData.tokenType());
        })
        .verifyComplete();
  }

  @Test
  void shouldFailWhenRegisteringUserWithExistingEmail() {
    // given
    final UserRegistration userRegistrationRequest = new UserRegistration(
        "test@example.com",
        "Password123!",
        "Password123!"
    );

    // Register first time
    keycloakIntegrationService.registerUser(userRegistrationRequest).block();

    // when & then - try to register again
    StepVerifier.create(keycloakIntegrationService.registerUser(userRegistrationRequest))
        .expectErrorMatches(throwable ->
            throwable instanceof EmailAlreadyRegisteredException &&
                "Email test@example.com is already registered in the system"
                    .equals(throwable.getMessage())
        )
        .verify();
  }

  @Test
  void shouldSuccessfullyAuthenticateUser() {
    // given
    final UserRegistration userRegistrationRequest = new UserRegistration(
        "auth-test@example.com",
        "Password123!",
        "Password123!"
    );
    keycloakIntegrationService.registerUser(userRegistrationRequest).block();

    final Credentials credentials = new Credentials(
        userRegistrationRequest.email(),
        userRegistrationRequest.password()
    );

    // when & then
    StepVerifier.create(keycloakIntegrationService.authenticateUser(credentials))
        .assertNext(authData -> {
          assertNotNull(authData.accessToken());
          assertNotNull(authData.refreshToken());
          assertTrue(authData.expiresIn() > 0);
          assertEquals("Bearer", authData.tokenType());
        })
        .verifyComplete();
  }

  @Test
  void shouldFailToAuthenticateWithInvalidCredentials() {
    // given
    final Credentials invalidCredentials = new Credentials(
        "nonexistent@example.com",
        "WrongPassword123!"
    );

    // when & then
    StepVerifier.create(keycloakIntegrationService.authenticateUser(invalidCredentials))
        .expectError(UnauthorizedCredentialsException.class)
        .verify();
  }
} 