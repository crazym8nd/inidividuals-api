package com.bnm.individuals_api.service;

import com.bnm.individuals_api.exception.InvalidRefreshToken;
import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.RefreshToken;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

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

  @Override
  public Mono<AuthData> refreshAccessToken(final RefreshToken request) {
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
          throw new InvalidRefreshToken("Invalid refreshToken", e);
        });
  }
}
