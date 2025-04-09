package com.bnm.individuals_api.dto;

public record UserRegistration(
    String email,
    String password,
    String confirmPassword) {

}
