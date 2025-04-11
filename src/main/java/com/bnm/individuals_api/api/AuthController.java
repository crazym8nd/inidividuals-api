package com.bnm.individuals_api.api;

import com.bnm.individuals_api.dto.SuccessUserRegistration;
import com.bnm.individuals_api.dto.UserRegistrationRequest;
import com.bnm.individuals_api.dto.UserRegistrationResponse;
import com.bnm.individuals_api.mapper.DtoMapper;
import com.bnm.individuals_api.service.KeycloakService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
@Slf4j
public class AuthController implements AuthApi {

  private final KeycloakService keycloakService;

  @Override
  @PostMapping("/test")
  public Mono<UserRegistrationResponse> registerUser(
      @RequestBody @Nonnull final UserRegistrationRequest request) {
    return keycloakService.registerUser(DtoMapper.mapFromRequest(request))
        .map(DtoMapper::mapToResponse);
  }
}
