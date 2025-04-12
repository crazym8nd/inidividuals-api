package com.bnm.individuals_api.dto;

public record LoginRequest(
    String email,
    String password
) {

}
