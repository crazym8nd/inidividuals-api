package com.bnm.individuals_api.service;

import com.bnm.individuals_api.dto.LoginRequest;
import com.bnm.individuals_api.dto.RefreshTokenRequest;
import com.bnm.individuals_api.dto.SuccessAuthResponse;
import reactor.core.publisher.Mono;

public interface AuthService {

  Mono<SuccessAuthResponse> authenticateUser(LoginRequest request);

  Mono<SuccessAuthResponse> refreshAccessToken(RefreshTokenRequest request);
}
