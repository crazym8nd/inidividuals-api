package com.bnm.individuals_api.service;

import com.bnm.individuals_api.dto.SuccessUserRegistration;
import com.bnm.individuals_api.dto.UserRegistration;
import reactor.core.publisher.Mono;

public interface KeycloakService {

  Mono<SuccessUserRegistration> registerUser(UserRegistration userRegistration);
}
