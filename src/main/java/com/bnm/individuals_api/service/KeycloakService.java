package com.bnm.individuals_api.service;

import com.bnm.individuals_api.dto.SuccessAuthResponse;
import com.bnm.individuals_api.dto.SuccessUserRegistration;
import com.bnm.individuals_api.dto.UserRegistration;
import reactor.core.publisher.Mono;

public interface KeycloakService {

  Mono<SuccessAuthResponse> registerUser(UserRegistration userRegistration);
}
