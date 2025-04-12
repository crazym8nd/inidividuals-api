package com.bnm.individuals_api.service;

import com.bnm.individuals_api.dto.SuccessAuthResponse;
import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.RefreshToken;
import reactor.core.publisher.Mono;

public interface AuthService {

  Mono<AuthData> authenticateUser(Credentials credentials);

  Mono<AuthData> refreshAccessToken(RefreshToken request);
}
