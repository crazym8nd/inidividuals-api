package com.bnm.individuals_api.service;

import com.bnm.individuals_api.dto.SuccessAuthResponse;
import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.RefreshToken;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
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


  public Keycloak keycloakForAuth(final Credentials credentials) {
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
    try {
      if (credentials.email() == null || credentials.password() == null) {
        return Mono.error(new IllegalArgumentException("Invalid credentials"));
      }
      final AccessTokenResponse token = keycloakForAuth(credentials).tokenManager()
          .getAccessToken();

      return Mono.just(new AuthData(
          token.getToken(),
          (int) token.getExpiresIn(),
          token.getRefreshToken(),
          token.getTokenType()
      ));
    } catch (final Exception e) {
      return Mono.error(new IllegalArgumentException("Invalid credentials"));
    }
  }

  @Override
  public Mono<AuthData> refreshAccessToken(final RefreshToken request) {
    try {
      if (request == null || request.refreshToken() == null) {
        return Mono.error(new IllegalArgumentException("Invalid refresh token"));
      }
      log.debug(request.refreshToken());
      final WebClient webClient = WebClient.builder().build();

      return webClient.post()
          .uri(authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token")
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(BodyInserters.fromFormData("grant_type", "refresh_token")
              .with("client_id", clientId)
              .with("client_secret", clientSecret)
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
            log.error("Refresh token failed");
            return Mono.error(new IllegalArgumentException("Token refresh failed"));
          });
    } catch (final Exception e) {
      log.error("Refresh token processing error", e);
      return Mono.error(new IllegalArgumentException("Token refresh failed"));
    }
  }
}
