package com.bnm.individuals_api.service;

import com.bnm.individuals_api.dto.UserRegistration;

public interface ValidationService {

  void validate(UserRegistration userRegistration);

  boolean isValid(UserRegistration userRegistration);
}
