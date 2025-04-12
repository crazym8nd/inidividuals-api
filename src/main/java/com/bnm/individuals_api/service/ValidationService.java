package com.bnm.individuals_api.service;

import com.bnm.individuals_api.model.UserRegistration;

public interface ValidationService {

  void validateEmail(String email);

  void validatePassword(String password, String confirmPassword);

  boolean isValid(UserRegistration userRegistration);
}
