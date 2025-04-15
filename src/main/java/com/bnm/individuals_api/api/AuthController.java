package com.bnm.individuals_api.api;

import com.bnm.individuals_api.dto.AboutMeResponse;
import com.bnm.individuals_api.dto.LoginRequest;
import com.bnm.individuals_api.dto.RefreshTokenRequest;
import com.bnm.individuals_api.dto.SuccessAuthResponse;
import com.bnm.individuals_api.dto.UserRegistrationRequest;
import com.bnm.individuals_api.mapper.AuthMapper;
import com.bnm.individuals_api.mapper.UserMapper;
import com.bnm.individuals_api.service.TokenService;
import com.bnm.individuals_api.service.UserService;
import jakarta.annotation.Nonnull;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

  private final UserService userService;
  private final TokenService tokenService;
  private final AuthMapper authMapper;
  private final UserMapper userMapper;

  @Override
  @PostMapping("/registration")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<SuccessAuthResponse> registerUser(
      @RequestBody @Nonnull final UserRegistrationRequest request) {
    return userService.registerUser(authMapper.toUserRegistration(request))
        .map(authMapper::toSuccessAuthResponse);
  }

  @Override
  @PostMapping("/login")
  public Mono<SuccessAuthResponse> loginUser(
      @RequestBody @Nonnull final LoginRequest request) {
    return userService.authenticateUser(authMapper.toCredentials(request))
        .map(authMapper::toSuccessAuthResponse);
  }

  @Override
  @GetMapping("/me")
  public Mono<AboutMeResponse> aboutMe(final Principal principal) {
    return userService.aboutMe(principal)
        .map(userMapper::toAboutMeResponse);
  }

  @Override
  @PostMapping("/refresh-token")
  public Mono<SuccessAuthResponse> refreshToken(
      @RequestBody @Nonnull final RefreshTokenRequest request) {
    return tokenService.refreshAccessToken(authMapper.toRefreshToken(request))
        .map(authMapper::toSuccessAuthResponse);
  }
}
