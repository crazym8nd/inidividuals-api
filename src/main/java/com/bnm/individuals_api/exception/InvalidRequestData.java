package com.bnm.individuals_api.exception;

public class InvalidRequestData extends RuntimeException {

  public InvalidRequestData(final String message) {
    super(message);
  }
}
