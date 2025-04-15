package com.bnm.individuals_api.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {

  public EmailAlreadyRegisteredException(String message) {
    super(message);
  }
}
