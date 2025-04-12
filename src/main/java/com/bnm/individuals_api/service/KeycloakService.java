package com.bnm.individuals_api.service;

import com.bnm.individuals_api.dto.SuccessAuthResponse;
import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.UserRegistration;
import reactor.core.publisher.Mono;

public interface KeycloakService {

  Mono<AuthData> registerUser(UserRegistration userRegistration);
}
