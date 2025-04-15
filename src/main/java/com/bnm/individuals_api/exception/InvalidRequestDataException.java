package com.bnm.individuals_api.exception;

public class InvalidRequestDataException extends RuntimeException {

  public InvalidRequestDataException(final String message) {
    super(message);
  }
}
