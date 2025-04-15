package com.bnm.individuals_api.service;

import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.RefreshToken;
import com.bnm.individuals_api.model.UserRegistration;
import reactor.core.publisher.Mono;

public interface KeycloakIntegrationService {
// здесь уже прям вызовы в кейклок се

  Mono<AuthData> refreshAccessToken(RefreshToken request);

  Mono<AuthData> registerUser(UserRegistration userRegistration);

  Mono<AuthData> authenticateUser(Credentials credentials);

}
