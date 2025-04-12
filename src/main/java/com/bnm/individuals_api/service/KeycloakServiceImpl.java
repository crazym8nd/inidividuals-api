package com.bnm.individuals_api.service;

import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.UserRegistration;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

  private final ValidationService validationService;
  private final Keycloak keycloak;
  private final AuthService authService;
  @Value("${keycloak.realm}")
  private String realm;

  @Override
  public Mono<AuthData> registerUser(final UserRegistration userRegistration) {
    if (validationService.isValid(userRegistration)) {

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
      if (!Objects.isNull(usersResource)) {
        final Response response = usersResource.create(user);

        log.debug(response.toString());
        if (response.getStatus() != 201) {
          return Mono.error(new IllegalArgumentException("Invalid credentials"));
        }
        final URI uri = response.getLocation();

        final String createdUserId = uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1);
        log.info("Created user {}", createdUserId);

        final RolesResource rolesResource = keycloak.realm(realm).roles();
        final RoleRepresentation representation = rolesResource.get("INDIVIDUALS")
            .toRepresentation();

        final UserResource userResource = keycloak.realm(realm).users().get(createdUserId);
        userResource.roles().realmLevel().add(Collections.singletonList(representation));

        return authService.authenticateUser(new Credentials(userRegistration.email(),
            userRegistration.password()));
      }
    }

    return Mono.empty();
  }
}
