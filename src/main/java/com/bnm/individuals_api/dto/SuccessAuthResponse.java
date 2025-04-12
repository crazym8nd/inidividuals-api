package com.bnm.individuals_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SuccessAuthResponse(
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("expires_in")
    Integer expiresIn,
    @JsonProperty("refresh_token")
    String refreshToken,
    @JsonProperty("token_type")
    String tokenType
) {

}
