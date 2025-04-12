package com.bnm.individuals_api.mapper;

import com.bnm.individuals_api.dto.LoginRequest;
import com.bnm.individuals_api.dto.RefreshTokenRequest;
import com.bnm.individuals_api.dto.SuccessAuthResponse;
import com.bnm.individuals_api.dto.UserRegistrationRequest;
import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.RefreshToken;
import com.bnm.individuals_api.model.UserRegistration;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthMapper {
    
    Credentials toCredentials(LoginRequest loginRequest);
    
    UserRegistration toUserRegistration(UserRegistrationRequest registrationRequest);
    
    RefreshToken toRefreshToken(RefreshTokenRequest refreshTokenRequest);
    
    SuccessAuthResponse toSuccessAuthResponse(AuthData authData);
} 