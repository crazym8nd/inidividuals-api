package com.bnm.individuals_api.dto;

public record SuccessAuthResponse(
    String accessToken,
    Integer expiresIn,
    String refreshToken,
    String tokenType
) {

}
