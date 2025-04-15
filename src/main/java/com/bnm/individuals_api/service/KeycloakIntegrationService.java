package com.bnm.individuals_api.service;

import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.RefreshToken;
import com.bnm.individuals_api.model.UserRegistration;
import reactor.core.publisher.Mono;

/**
 * Сервис для интеграции c Keycloak.
 * Содержит методы, обращающиеся к конкретным API Keycloak сервера.
 */
public interface KeycloakIntegrationService {

    /**
     * Обновляет access token используя refresh token.
     *
     * @param request объект содержащий refresh token
     * @see AuthData
     */
    Mono<AuthData> refreshAccessToken(RefreshToken request);

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param userRegistration данные для регистрации нового пользователя
     * @see AuthData
     */
    Mono<AuthData> registerUser(UserRegistration userRegistration);

    /**
     * Аутентифицирует пользователя по учетным данным.
     *
     * @param credentials учетные данные пользователя
     * @see AuthData
     */
    Mono<AuthData> authenticateUser(Credentials credentials);

}
