package com.bnm.individuals_api.service;

import com.bnm.individuals_api.dto.SuccessUserRegistration;
import com.bnm.individuals_api.dto.UserRegistrationRequest;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

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


  public Keycloak keycloakForAuth(final UserRegistrationRequest request) {
    return KeycloakBuilder.builder()
        .serverUrl(authServerUrl)
        .realm(realm)
        .grantType(OAuth2Constants.PASSWORD)
        .clientId(clientId)
        .clientSecret(clientSecret)
        .username(request.email())
        .password(request.password())
        .build();
  }

  @Override
  public Mono<SuccessUserRegistration> getToken(final UserRegistrationRequest request) {
    try {
      if (request.email() == null || request.password() == null) {
        return Mono.error(new IllegalArgumentException("Invalid credentials"));
      }
      final AccessTokenResponse token = keycloakForAuth(request).tokenManager().getAccessToken();

      return Mono.just(new SuccessUserRegistration(
          token.getToken(),
          (int) token.getExpiresIn(),
          token.getRefreshToken(),
          token.getTokenType()
      ));
    } catch (final Exception e) {
      return Mono.error(new IllegalArgumentException("Invalid credentials"));
    }
  }
}
