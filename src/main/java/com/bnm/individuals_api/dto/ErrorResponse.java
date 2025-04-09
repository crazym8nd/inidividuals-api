package com.bnm.individuals_api.dto;


public record ErrorResponse(
    String error,
    Integer status) {

}
