package com.bnm.individuals_api.service;

import com.bnm.individuals_api.dto.SuccessAuthResponse;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.RefreshToken;
import reactor.core.publisher.Mono;

public interface AuthService {

  Mono<SuccessAuthResponse> authenticateUser(Credentials credentials);

  Mono<SuccessAuthResponse> refreshAccessToken(RefreshToken request);
}
