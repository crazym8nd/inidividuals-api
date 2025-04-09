package com.bnm.individuals_api.service;

import com.bnm.individuals_api.dto.SuccessUserRegistration;
import com.bnm.individuals_api.dto.UserRegistration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

  private final ValidationService validationService;

  @Override
  public SuccessUserRegistration registerUser(final UserRegistration userRegistration) {
    validationService.validate(userRegistration);
    return new SuccessUserRegistration(
        "asdasefrtbtyberf43t56hytbtrgbf",
        3600,
        "referwfcrtbyntyhn",
        "Bearer"
    );
  }
}
