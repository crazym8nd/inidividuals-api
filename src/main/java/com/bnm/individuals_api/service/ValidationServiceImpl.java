package com.bnm.individuals_api.service;

import com.bnm.individuals_api.exception.InvalidRequestData;
import com.bnm.individuals_api.model.UserRegistration;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ValidationServiceImpl implements ValidationService {

  final UserRegistration hardcodedValidation = new UserRegistration(
      "asd@asd.com",
      "qwerty",
      "qwerty"
  );

  @Override
  public void hardcodedValidation(final UserRegistration userRegistration) {
    if (!Objects.equals(userRegistration, hardcodedValidation)) {
      log.info("Data is invalid: '{}'", userRegistration.toString());
      throw new InvalidRequestData(
          "Data is invalid:  '%s' ".formatted(userRegistration.toString()));
    }
  }

  @Override
  public boolean isValid(final UserRegistration userRegistration) {
    return Objects.equals(userRegistration, hardcodedValidation);
  }
}
