package com.bnm.individuals_api.mapper;

import com.bnm.individuals_api.dto.SuccessUserRegistration;
import com.bnm.individuals_api.dto.UserRegistration;
import com.bnm.individuals_api.dto.UserRegistrationRequest;
import com.bnm.individuals_api.dto.UserRegistrationResponse;

public class DtoMapper {

  public static UserRegistration mapFromRequest(final UserRegistrationRequest request) {
    return new UserRegistration(request.email(), request.password(), request.confirmPassword());
  }

  public static UserRegistrationResponse mapToResponse(
      final SuccessUserRegistration successUserRegistration) {
    return new UserRegistrationResponse(successUserRegistration.accessToken(),
        successUserRegistration.expiresIn(), successUserRegistration.refreshToken(),
        successUserRegistration.tokenType());
  }

}
