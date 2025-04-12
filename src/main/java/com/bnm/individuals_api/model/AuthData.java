package com.bnm.individuals_api.model;

public record AuthData(
    String accessToken,
    Integer expiresIn,
    String refreshToken,
    String tokenType
) {

}
