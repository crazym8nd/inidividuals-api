package com.bnm.individuals_api.dto;

public record UserRegistrationRequest(
    String email,
    String password,
    String confirmPassword) {

}
