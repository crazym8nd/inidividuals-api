package com.bnm.individuals_api.service;

import com.bnm.individuals_api.configuration.KeycloakIntegrationExternalServiceProperties;
import com.bnm.individuals_api.exception.EmailAlreadyRegisteredException;
import com.bnm.individuals_api.exception.InvalidRefreshToken;
import com.bnm.individuals_api.exception.UnauthorizedCredentialsException;
import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.RefreshToken;
import com.bnm.individuals_api.model.UserRegistration;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(KeycloakIntegrationExternalServiceProperties.class)
public class KeycloakIntegrationServiceImpl implements KeycloakIntegrationService {

  private final KeycloakIntegrationExternalServiceProperties properties;

  private Keycloak getAdminKeycloak() {
    return KeycloakBuilder.builder()
        .serverUrl(properties.authUrl())
        .realm(properties.realm())
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .clientId(properties.clientId())
        .clientSecret(properties.clientSecret())
        .build();
  }

  private Keycloak getPasswordGrantKeycloak(final Credentials credentials) {
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

  @Override
  public Mono<AuthData> refreshAccessToken(final RefreshToken request) {
    if (request == null || request.refreshToken() == null) {
      return Mono.error(new IllegalArgumentException("Invalid refresh token"));
    }
    log.debug(request.refreshToken());
    final WebClient webClient = WebClient.builder().build();

    return webClient.post()
        .uri(properties.authUrl() + "/realms/" + properties.realm()
            + "/protocol/openid-connect/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData("grant_type", "refresh_token")
            .with("client_id", properties.clientId())
            .with("client_secret", properties.clientSecret())
            .with("refresh_token", request.refreshToken()))
        .retrieve()
        .bodyToMono(AccessTokenResponse.class)
        .map(tokenResponse -> new AuthData(
            tokenResponse.getToken(),
            (int) tokenResponse.getExpiresIn(),
            tokenResponse.getRefreshToken(),
            tokenResponse.getTokenType()
        ))
        .onErrorResume(e -> {
          throw new InvalidRefreshToken("Invalid refreshToken", e);
        });
  }

  @Override
  public Mono<AuthData> registerUser(final UserRegistration userRegistration) {
    final UserRepresentation user = new UserRepresentation();
    user.setEnabled(true);
    user.setUsername(userRegistration.email());
    user.setEmail(userRegistration.email());
    user.setFirstName("firstName");
    user.setLastName("lastName");
    user.setEmailVerified(true);

    final CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
    credentialRepresentation.setValue(userRegistration.password());
    credentialRepresentation.setTemporary(false);
    credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

    final List<CredentialRepresentation> list = new ArrayList<>();
    list.add(credentialRepresentation);
    user.setCredentials(list);

    final Keycloak adminKeycloak = getAdminKeycloak();
    final UsersResource usersResource = adminKeycloak.realm(properties.realm()).users();
    Response response = null;
    if (!Objects.isNull(usersResource)) {
      try {
        response = usersResource.create(user);
      } catch (final Exception e) {
        log.info(e.getMessage(), e);
      }

      if (Objects.requireNonNull(response).getStatus() == 409) {
        throw new EmailAlreadyRegisteredException("Данный email уже зарегистрирован в системе");
      }
      final URI uri = response.getLocation();

      final String createdUserId = uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1);
      log.info("Created user {}", createdUserId);

      final RolesResource rolesResource = adminKeycloak.realm(properties.realm()).roles();
      final RoleRepresentation representation = rolesResource.get("INDIVIDUALS")
          .toRepresentation();

      final UserResource userResource = adminKeycloak.realm(properties.realm()).users()
          .get(createdUserId);
      userResource.roles().realmLevel().add(Collections.singletonList(representation));

      return authenticateUser(new Credentials(userRegistration.email(),
          userRegistration.password()));
    }

    return Mono.empty();
  }

  @Override
  public Mono<AuthData> authenticateUser(final Credentials credentials) {
    try {
      final AccessTokenResponse token = getPasswordGrantKeycloak(credentials).tokenManager()
          .getAccessToken();

      return Mono.just(new AuthData(
          token.getToken(),
          (int) token.getExpiresIn(),
          token.getRefreshToken(),
          token.getTokenType()
      ));
    } catch (final Exception e) {
      throw new UnauthorizedCredentialsException(e.getMessage(), e);
    }
  }
}
