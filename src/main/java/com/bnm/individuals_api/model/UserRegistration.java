package com.bnm.individuals_api.model;

public record UserRegistration(
    String email,
    String password,
    String confirmPassword) {

}
