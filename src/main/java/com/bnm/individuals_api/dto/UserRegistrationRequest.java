package com.bnm.individuals_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserRegistrationRequest(
    String email,
    String password,
    @JsonProperty("confirm_password")
    String confirmPassword) {

}
