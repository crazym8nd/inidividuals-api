package com.bnm.individuals_api.service;

import com.bnm.individuals_api.dto.SuccessUserRegistration;
import com.bnm.individuals_api.dto.UserRegistrationRequest;
import reactor.core.publisher.Mono;

public interface AuthService {

  Mono<SuccessUserRegistration> getToken(UserRegistrationRequest request);
}
