package com.bnm.individuals_api.service;

import com.bnm.individuals_api.dto.SuccessUserRegistration;
import com.bnm.individuals_api.dto.UserRegistration;

public interface KeycloakService {

  SuccessUserRegistration registerUser(UserRegistration userRegistration);
}
