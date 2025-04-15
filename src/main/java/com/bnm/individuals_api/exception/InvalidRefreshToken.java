package com.bnm.individuals_api.exception;

public class InvalidRefreshToken extends RuntimeException {

  public InvalidRefreshToken(String message) {
    super(message);
  }

  public InvalidRefreshToken(String message, Throwable cause) {
    super(message, cause);
  }
}
