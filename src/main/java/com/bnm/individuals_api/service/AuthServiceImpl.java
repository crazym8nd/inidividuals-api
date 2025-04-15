package com.bnm.individuals_api.service;

import com.bnm.individuals_api.configuration.KeycloakUserDetails;
import com.bnm.individuals_api.exception.InvalidRefreshToken;
import com.bnm.individuals_api.exception.UnauthorizedCredentialsException;
import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.RefreshToken;
import com.bnm.individuals_api.model.UserData;
import java.security.Principal;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final ValidationService validationService;

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
}
