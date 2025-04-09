package com.bnm.individuals_api.dto;

public record SuccessUserRegistration(
    String accessToken,
    Integer expiresIn,
    String refreshToken,
    String tokenType) {

}
