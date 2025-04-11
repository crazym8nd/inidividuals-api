package com.bnm.individuals_api.api;

import com.bnm.individuals_api.dto.AboutMeResponse;
import com.bnm.individuals_api.dto.RefreshTokenRequest;
import com.bnm.individuals_api.dto.SuccessUserRegistration;
import com.bnm.individuals_api.dto.UserRegistrationRequest;
import com.bnm.individuals_api.dto.UserRegistrationResponse;
import com.bnm.individuals_api.mapper.DtoMapper;
import com.bnm.individuals_api.service.AuthService;
import com.bnm.individuals_api.service.KeycloakService;
import jakarta.annotation.Nonnull;
import java.security.Principal;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
@Slf4j
public class AuthController implements AuthApi {

  private final KeycloakService keycloakService;
  private final AuthService authService;

  @Override
  @PostMapping("/test")
  public UserRegistrationResponse testEndpoint(
      @RequestBody @Nonnull final UserRegistrationRequest request) {
    final var test = keycloakService.testEnpoint(DtoMapper.mapFromRequest(request));
    return DtoMapper.mapToResponse(Objects.requireNonNull(test.block()));
  }

  @Override
  @PostMapping("/registration")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<UserRegistrationResponse> registerUser(
      @RequestBody @Nonnull final UserRegistrationRequest request) {
    return keycloakService.registerUser(DtoMapper.mapFromRequest(request))
        .map(response -> new UserRegistrationResponse("success", 3600, response, "BEARER"));
  }

  @Override
  @PostMapping("/login")
  public Mono<SuccessUserRegistration> loginUser(
      @RequestBody @Nonnull final UserRegistrationRequest request) {
    return authService.getToken(request);
  }

  @Override
  @GetMapping("/me")
  public Mono<AboutMeResponse> aboutMe(final Principal principal) {

    final Authentication authentication = (Authentication) principal;
    final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    final Set<String> roleList = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toUnmodifiableSet());

    return Mono.just(new AboutMeResponse(
        principal.getName() + "userId",
        principal.getName() + "mail",
        roleList,
        Instant.now()
    ));
  }

  @Override
  @PostMapping("/refresh-token")
  public Mono<SuccessUserRegistration> refreshToken(
      @RequestBody @Nonnull final RefreshTokenRequest request) {
    final var response = authService.refreshToken(request);
    return response;
  }
}
