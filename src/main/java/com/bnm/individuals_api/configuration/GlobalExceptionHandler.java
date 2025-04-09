package com.bnm.individuals_api.configuration;

import com.bnm.individuals_api.dto.ErrorResponse;
import com.bnm.individuals_api.exception.InvalidRequestData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InvalidRequestData.class)
  public ResponseEntity<ErrorResponse> handleInvalidRequestDataException(final InvalidRequestData ex) {
    final ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 400);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }
}
