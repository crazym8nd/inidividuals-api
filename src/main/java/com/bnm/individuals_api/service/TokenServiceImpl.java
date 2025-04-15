package com.bnm.individuals_api.service;

import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.RefreshToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

  private final KeycloakIntegrationService keycloakIntegrationService;

  @Override
  public Mono<AuthData> refreshAccessToken(final RefreshToken request) {
    return keycloakIntegrationService.refreshAccessToken(request);
  }
}
