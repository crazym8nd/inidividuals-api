package com.bnm.individuals_api.service;

import com.bnm.individuals_api.configuration.KeycloakUserDetails;
import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.UserData;
import com.bnm.individuals_api.model.UserRegistration;
import java.security.Principal;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final ValidationService validationService;
  private final KeycloakIntegrationService keycloakIntegrationService;


  @Override
  public Mono<AuthData> authenticateUser(final Credentials credentials) {
    validationService.validateEmail(credentials.email());
    return keycloakIntegrationService.authenticateUser(credentials);
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
    validationService.validateUserRegistration(userRegistration);
    return keycloakIntegrationService.registerUser(userRegistration);
  }
}
