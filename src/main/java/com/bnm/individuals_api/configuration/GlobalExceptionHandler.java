package com.bnm.individuals_api.configuration;

import com.bnm.individuals_api.dto.ErrorResponse;
import com.bnm.individuals_api.exception.EmailAlreadyRegisteredException;
import com.bnm.individuals_api.exception.InvalidRefreshToken;
import com.bnm.individuals_api.exception.InvalidRequestDataException;
import com.bnm.individuals_api.exception.UnauthorizedCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InvalidRequestDataException.class)
  public ResponseEntity<ErrorResponse> handleInvalidRequestDataException(
      final InvalidRequestDataException ex) {
    final ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 400);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(EmailAlreadyRegisteredException.class)
  public ResponseEntity<ErrorResponse> handleEmailAlreadyRegisteredException(
      final EmailAlreadyRegisteredException ex) {
    final ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 409);
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(UnauthorizedCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedCredentialsException(
      final UnauthorizedCredentialsException ex) {
    final ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 401);
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(InvalidRefreshToken.class)
  public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(
      final InvalidRefreshToken ex) {
    final ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 401);
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }
}
