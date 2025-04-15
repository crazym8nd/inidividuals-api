package com.bnm.individuals_api.service;

import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.RefreshToken;
import com.bnm.individuals_api.model.UserData;
import java.security.Principal;
import reactor.core.publisher.Mono;

/**
 * Сервис аутентификации и авторизации пользователей.
 */
public interface AuthService {

  /**
   * Аутентифицирует пользователя по предоставленным учетным данным.
   *
   * @param credentials учетные данные пользователя
   * @see AuthData
   */
  Mono<AuthData> authenticateUser(Credentials credentials);

  /**
   * Обновляет access token с помощью refresh token.
   *
   * @param request объект, содержащий refresh token
   * @see AuthData
   */
  Mono<AuthData> refreshAccessToken(RefreshToken request);

  /**
   * Получает информацию о текущем аутентифицированном пользователе.
   *
   * @param principal объект, представляющий текущего аутентифицированного пользователя
   * @see UserData
   */
  Mono<UserData> aboutMe(Principal principal);
}
