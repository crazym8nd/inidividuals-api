package com.bnm.individuals_api.service;

import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.UserData;
import com.bnm.individuals_api.model.UserRegistration;
import java.security.Principal;
import reactor.core.publisher.Mono;

/**
 * Сервис аутентификации и авторизации пользователей.
 */
public interface UserService {

  /**
   * Аутентифицирует пользователя по предоставленным учетным данным.
   *
   * @param credentials учетные данные пользователя
   * @see AuthData
   */
  Mono<AuthData> authenticateUser(Credentials credentials);

  /**
   * Получает информацию о текущем аутентифицированном пользователе.
   *
   * @param principal объект, представляющий текущего аутентифицированного пользователя
   * @see UserData
   */
  Mono<UserData> aboutMe(Principal principal);

  /**
   * Регистрирует нового пользователя в Keycloak.
   *
   * @param userRegistration данные для регистрации пользователя
   * @see AuthData
   */
  Mono<AuthData> registerUser(UserRegistration userRegistration);
}
