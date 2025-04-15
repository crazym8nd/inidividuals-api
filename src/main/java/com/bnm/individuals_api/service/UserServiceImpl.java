package com.bnm.individuals_api.service;

import com.bnm.individuals_api.configuration.KeycloakUserDetails;
import com.bnm.individuals_api.exception.EmailAlreadyRegisteredException;
import com.bnm.individuals_api.exception.UnauthorizedCredentialsException;
import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.UserData;
import com.bnm.individuals_api.model.UserRegistration;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final ValidationService validationService;
  private final Keycloak keycloak;
  @Value("${keycloak.realm}")
  @Setter
  private String realm;
  @Value("${keycloak.urls.auth}")
  @Setter
  private String authServerUrl;
  @Value("${keycloak.clientId}")
  @Setter
  private String clientId;
  @Value("${keycloak.clientSecret}")
  @Setter
  private String clientSecret;

  private Keycloak keycloakForAuth(final Credentials credentials) {
    return KeycloakBuilder.builder()
        .serverUrl(authServerUrl)
        .realm(realm)
        .grantType(OAuth2Constants.PASSWORD)
        .clientId(clientId)
        .clientSecret(clientSecret)
        .username(credentials.email())
        .password(credentials.password())
        .build();
  }

  @Override
  public Mono<AuthData> authenticateUser(final Credentials credentials) {
    validationService.validateEmail(credentials.email());
    try {
      final AccessTokenResponse token = keycloakForAuth(credentials).tokenManager()
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


  @Override
  public Mono<UserData> aboutMe(final Principal principal) {
    return Mono.just(principal)
        .cast(JwtAuthenticationToken.class)
        .map(jwtAuth -> {
          final KeycloakUserDetails userDetails = new KeycloakUserDetails(jwtAuth.getToken());
          return new UserData(
              userDetails.getId(),
              userDetails.getEmail(),
              userDetails.getAuthorities().stream()
                  .map(GrantedAuthority::getAuthority)
                  .collect(Collectors.toUnmodifiableSet()),
              userDetails.getCreatedAt()
          );
        });
  }

  @Override
  public Mono<AuthData> registerUser(final UserRegistration userRegistration) {
    validationService.validate(userRegistration);

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

    final UsersResource usersResource = keycloak.realm(realm).users();
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

      final RolesResource rolesResource = keycloak.realm(realm).roles();
      final RoleRepresentation representation = rolesResource.get("INDIVIDUALS")
          .toRepresentation();

      final UserResource userResource = keycloak.realm(realm).users().get(createdUserId);
      userResource.roles().realmLevel().add(Collections.singletonList(representation));

      return authenticateUser(new Credentials(userRegistration.email(),
          userRegistration.password()));
    }

    return Mono.empty();
  }
}
