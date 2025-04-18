package com.bnm.individuals_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bnm.individuals_api.config.KeycloakTestContainers;
import com.bnm.individuals_api.configuration.KeycloakIntegrationExternalServiceProperties;
import com.bnm.individuals_api.exception.EmailAlreadyRegisteredException;
import com.bnm.individuals_api.exception.InvalidRefreshToken;
import com.bnm.individuals_api.exception.UnauthorizedCredentialsException;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.RefreshToken;
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

  private static final String TEST_EMAIL = "test@example.com";
  private static final String TEST_PASSWORD = "Password123!";
  private static final String TEST_AUTH_EMAIL = "auth-test@example.com";
  private static final String INVALID_EMAIL = "not-an-email";
  private static final String INVALID_REFRESH_TOKEN = "invalid-refresh-token";

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
  void registerUser_WithValidData_ShouldSuccessfullyCreateUser() {
    final UserRegistration userRegistrationRequest = new UserRegistration(
        TEST_EMAIL,
        TEST_PASSWORD,
        TEST_PASSWORD
    );

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
  void registerUser_WithExistingEmail_ShouldFailWithEmailAlreadyRegistered() {
    final UserRegistration userRegistrationRequest = new UserRegistration(
        TEST_EMAIL,
        TEST_PASSWORD,
        TEST_PASSWORD
    );

    keycloakIntegrationService.registerUser(userRegistrationRequest).block();

    StepVerifier.create(keycloakIntegrationService.registerUser(userRegistrationRequest))
        .expectErrorMatches(throwable ->
            throwable instanceof EmailAlreadyRegisteredException &&
                "Email test@example.com is already registered in the system"
                    .equals(throwable.getMessage())
        )
        .verify();
  }

  @Test
  void registerUser_WithInvalidEmail_ShouldFailWithValidationError() {
    final UserRegistration userRegistrationRequest = new UserRegistration(
        INVALID_EMAIL,
        TEST_PASSWORD,
        TEST_PASSWORD
    );

    StepVerifier.create(keycloakIntegrationService.registerUser(userRegistrationRequest))
        .expectError()
        .verify();
  }

  @Test
  void authenticateUser_WithValidCredentials_ShouldSuccessfullyAuthenticate() {
    final UserRegistration userRegistrationRequest = new UserRegistration(
        TEST_AUTH_EMAIL,
        TEST_PASSWORD,
        TEST_PASSWORD
    );
    keycloakIntegrationService.registerUser(userRegistrationRequest).block();

    final Credentials credentials = new Credentials(
        userRegistrationRequest.email(),
        userRegistrationRequest.password()
    );

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
  void authenticateUser_WithInvalidCredentials_ShouldFailWithUnauthorized() {
    final Credentials invalidCredentials = new Credentials(
        "nonexistent@example.com",
        "WrongPassword123!"
    );

    StepVerifier.create(keycloakIntegrationService.authenticateUser(invalidCredentials))
        .expectError(UnauthorizedCredentialsException.class)
        .verify();
  }

  @Test
  void refreshToken_WithValidToken_ShouldReturnNewAccessToken() {
    final UserRegistration userRegistration = new UserRegistration(
        TEST_EMAIL,
        TEST_PASSWORD,
        TEST_PASSWORD
    );

    final String refreshToken = keycloakIntegrationService.registerUser(userRegistration)
        .block()
        .refreshToken();

    StepVerifier.create(
            keycloakIntegrationService.refreshAccessToken(new RefreshToken(refreshToken)))
        .assertNext(authData -> {
          assertNotNull(authData.accessToken());
          assertNotNull(authData.refreshToken());
          assertTrue(authData.expiresIn() > 0);
          assertEquals("Bearer", authData.tokenType());
        })
        .verifyComplete();
  }

  @Test
  void refreshToken_WithInvalidToken_ShouldFailWithInvalidRefreshToken() {
    StepVerifier.create(keycloakIntegrationService.refreshAccessToken(
            new RefreshToken(INVALID_REFRESH_TOKEN)))
        .expectError(InvalidRefreshToken.class)
        .verify();
  }
} 