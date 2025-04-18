package com.bnm.individuals_api.service;

import com.bnm.individuals_api.configuration.KeycloakIntegrationExternalServiceProperties;
import com.bnm.individuals_api.exception.EmailAlreadyRegisteredException;
import com.bnm.individuals_api.exception.InvalidRefreshToken;
import com.bnm.individuals_api.exception.UnauthorizedCredentialsException;
import com.bnm.individuals_api.exception.UserRegistrationException;
import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.RefreshToken;
import com.bnm.individuals_api.model.UserRegistration;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@Validated
@EnableConfigurationProperties(KeycloakIntegrationExternalServiceProperties.class)
@ConditionalOnProperty(
    value = "keycloak.enabled",
    havingValue = "true")
public class KeycloakIntegrationServiceImpl implements KeycloakIntegrationService {

  private static final String ROLE_INDIVIDUALS = "INDIVIDUALS";
  private static final String TOKEN_PATH = "/protocol/openid-connect/token";
  private static final String GRANT_TYPE_REFRESH = "refresh_token";

  private final KeycloakIntegrationExternalServiceProperties properties;
  private final WebClient webClient;

  @Override
  public Mono<AuthData> refreshAccessToken(final RefreshToken request) {
    return webClient.post()
        .uri(properties.authUrl() + "/realms/" + properties.realm() + TOKEN_PATH)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData("grant_type", GRANT_TYPE_REFRESH)
            .with("client_id", properties.clientId())
            .with("client_secret", properties.clientSecret())
            .with("refresh_token", request.refreshToken()))
        .retrieve()
        .bodyToMono(AccessTokenResponse.class)
        .map(tokenResponse -> {
          log.info("Successfully refreshed access token");
          return new AuthData(
              tokenResponse.getToken(),
              (int) tokenResponse.getExpiresIn(),
              tokenResponse.getRefreshToken(),
              tokenResponse.getTokenType()
          );
        })
        .onErrorResume(e -> {
          log.error("Failed to refresh access token: {}", e.getMessage());
          throw new InvalidRefreshToken(String.format("Invalid refresh token: %s", e.getMessage()), e);
        });
  }

  @Override
  public Mono<AuthData> registerUser(final UserRegistration userRegistration) {
    try {
      final UserRepresentation user = createUserRepresentation(userRegistration);
      final Keycloak adminKeycloak = getAdminClientKeycloak();
      final UsersResource usersResource = adminKeycloak.realm(properties.realm()).users();

      if (Objects.isNull(usersResource)) {
        log.error("Failed to get users resource for realm: {}", properties.realm());
        return Mono.error(new UserRegistrationException(String.format("Failed to get access to users resource for realm: %s", properties.realm())));
      }

      final Response response = usersResource.create(user);

      switch (response.getStatus()) {
        case 201 -> {
          final URI uri = response.getLocation();
          final String createdUserId = uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1);
          log.info("Successfully created user with ID: {}", createdUserId);

          assignRoleToUser(adminKeycloak, createdUserId);
          return authenticateUser(new Credentials(userRegistration.email(), userRegistration.password()));
        }
        case 409 -> {
          log.warn("User registration failed - email already exists: {}", userRegistration.email());
          throw new EmailAlreadyRegisteredException(String.format("Email %s is already registered in the system", userRegistration.email()));
        }
        default -> {
          log.error("User registration failed with status: {}", response.getStatus());
          throw new UserRegistrationException(String.format("User registration error. Status: %d", response.getStatus()));
        }
      }
    } catch (EmailAlreadyRegisteredException e) {
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error during user registration: {}", e.getMessage());
      throw new UserRegistrationException(String.format("Unexpected error during user registration: %s", e.getMessage()), e);
    }
  }

  private UserRepresentation createUserRepresentation(final UserRegistration userRegistration) {
    final UserRepresentation user = new UserRepresentation();
    user.setEnabled(true);
    user.setUsername(userRegistration.email());
    user.setEmail(userRegistration.email());
    user.setEmailVerified(true);

    final CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
    credentialRepresentation.setValue(userRegistration.password());
    credentialRepresentation.setTemporary(false);
    credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

    user.setCredentials(List.of(credentialRepresentation));
    return user;
  }

  private void assignRoleToUser(final Keycloak adminKeycloak, final String userId) {
    try {
      final RolesResource rolesResource = adminKeycloak.realm(properties.realm()).roles();
      final RoleRepresentation representation = rolesResource.get(ROLE_INDIVIDUALS)
          .toRepresentation();

      final UserResource userResource = adminKeycloak.realm(properties.realm()).users()
          .get(userId);
      userResource.roles().realmLevel().add(Collections.singletonList(representation));
      log.info("Successfully assigned role {} to user {}", ROLE_INDIVIDUALS, userId);
    } catch (final Exception e) {
      log.error("Failed to assign role to user {}: {}", userId, e.getMessage());
      throw new UserRegistrationException(String.format("Failed to assign role %s to user %s: %s", ROLE_INDIVIDUALS, userId, e.getMessage()), e);
    }
  }

  @Override
  public Mono<AuthData> authenticateUser(final Credentials credentials) {
    try {
      final AccessTokenResponse token = getAdminClientKeycloakWithUserCredentials(credentials)
          .tokenManager()
          .getAccessToken();

      log.info("Successfully authenticated user: {}", credentials.email());
      return Mono.just(new AuthData(
          token.getToken(),
          (int) token.getExpiresIn(),
          token.getRefreshToken(),
          token.getTokenType()
      ));
    } catch (final Exception e) {
      log.error("Authentication failed for user {}: {}", credentials.email(), e.getMessage());
      throw new UnauthorizedCredentialsException(String.format("Authentication failed for user %s: %s", credentials.email(), e.getMessage()), e);
    }
  }

  private Keycloak getAdminClientKeycloak() {
    return KeycloakBuilder.builder()
        .serverUrl(properties.authUrl())
        .realm(properties.realm())
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(properties.clientId())
        .clientSecret(properties.clientSecret())
        .build();
  }

  private Keycloak getAdminClientKeycloakWithUserCredentials(final Credentials credentials) {
    return KeycloakBuilder.builder()
        .serverUrl(properties.authUrl())
        .realm(properties.realm())
        .grantType(OAuth2Constants.PASSWORD)
        .clientId(properties.clientId())
        .clientSecret(properties.clientSecret())
        .username(credentials.email())
        .password(credentials.password())
        .build();
  }
}
