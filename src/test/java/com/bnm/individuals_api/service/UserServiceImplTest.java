package com.bnm.individuals_api.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.Credentials;
import com.bnm.individuals_api.model.UserData;
import com.bnm.individuals_api.model.UserRegistration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @InjectMocks
  private UserServiceImpl userService;

  @Mock
  private ValidationService validationService;

  @Mock
  private KeycloakIntegrationService keycloakIntegrationService;

  @Test
  void shouldAuthenticateUser() {
    // given
    Credentials credentials = new Credentials("test@example.com", "password123");
    AuthData expectedAuthData = new AuthData("access-token", 300, "refresh-token", "Bearer");

    given(keycloakIntegrationService.authenticateUser(credentials))
        .willReturn(Mono.just(expectedAuthData));

    // when
    Mono<AuthData> result = userService.authenticateUser(credentials);

    // then
    StepVerifier.create(result)
        .expectNext(expectedAuthData)
        .verifyComplete();

    verify(validationService).validateEmail(credentials.email());
    verify(keycloakIntegrationService).authenticateUser(credentials);
  }

  @Test
  void shouldRegisterUser() {
    // given
    UserRegistration registration = new UserRegistration(
        "test@example.com",
        "password123",
        "password123"
    );
    AuthData expectedAuthData = new AuthData("access-token", 300, "refresh-token", "Bearer");

    given(keycloakIntegrationService.registerUser(registration))
        .willReturn(Mono.just(expectedAuthData));

    // when
    Mono<AuthData> result = userService.registerUser(registration);

    // then
    StepVerifier.create(result)
        .expectNext(expectedAuthData)
        .verifyComplete();

    verify(validationService).validateUserRegistration(registration);
    verify(keycloakIntegrationService).registerUser(registration);
  }

  @Test
  void shouldReturnUserData() {
    // given
    String userId = "user-123";
    String email = "test@example.com";
    String username = "testuser";
    Set<String> roles = Set.of("USER");
    Instant createdAt = Instant.now();
    Long createdAtMillis = createdAt.toEpochMilli();

    Map<String, Object> realmAccess = Map.of(
        "roles", List.of("USER")
    );

    Jwt jwt = Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .claim("sub", userId)
        .claim("email", email)
        .claim("preferred_username", username)
        .claim("realm_access", realmAccess)
        .claim("createdTimestamp", createdAtMillis)
        .build();

    JwtAuthenticationToken principal = new JwtAuthenticationToken(jwt);

    UserData expectedUserData = new UserData(
        userId,
        email,
        Set.of("ROLE_USER"),
        Instant.ofEpochMilli(createdAtMillis)
    );

    // when
    Mono<UserData> result = userService.aboutMe(principal);

    // then
    StepVerifier.create(result)
        .expectNext(expectedUserData)
        .verifyComplete();
  }
} 