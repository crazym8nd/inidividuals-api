package com.bnm.individuals_api.mapper;

import com.bnm.individuals_api.dto.UserRegistrationRequest;
import com.bnm.individuals_api.model.UserRegistration;

public class DtoMapper {

  public static UserRegistration mapFromRequest(final UserRegistrationRequest request) {
    return new UserRegistration(request.email(), request.password(), request.confirmPassword());
  }

}
