package com.bnm.individuals_api.service;

import com.bnm.individuals_api.dto.SuccessAuthResponse;
import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.UserRegistration;
import reactor.core.publisher.Mono;

/**
 * Сервис для взаимодействия с Keycloak.
 */
public interface KeycloakService {

  /**
   * Регистрирует нового пользователя в Keycloak.
   *
   * @param userRegistration данные для регистрации пользователя
   * @see AuthData
   */
  Mono<AuthData> registerUser(UserRegistration userRegistration);
}
