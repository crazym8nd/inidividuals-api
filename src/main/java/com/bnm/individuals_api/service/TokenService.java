package com.bnm.individuals_api.service;

import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.RefreshToken;
import reactor.core.publisher.Mono;

public interface TokenService {

  /**
   * Обновляет access token с помощью refresh token.
   *
   * @param request объект, содержащий refresh token
   * @see AuthData
   */
  Mono<AuthData> refreshAccessToken(RefreshToken request);
}
