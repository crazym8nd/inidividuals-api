package com.bnm.individuals_api.dto;

public record UserRegistrationResponse(
    String accessToken,
    Integer expiresIn,
    String refreshToken,
    String tokenType) {

}
