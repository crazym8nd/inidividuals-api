package com.bnm.individuals_api.exception;

public class UnauthorizedCredentialsException extends RuntimeException {

  public UnauthorizedCredentialsException(String message) {
    super(message);
  }

  public UnauthorizedCredentialsException(String message, Throwable cause) {
    super(message, cause);
  }
}
