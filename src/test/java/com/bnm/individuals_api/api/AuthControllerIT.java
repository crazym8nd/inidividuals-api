package com.bnm.individuals_api.api;

import com.bnm.individuals_api.config.KeycloakTestContainers;
import com.bnm.individuals_api.dto.AboutMeResponse;
import com.bnm.individuals_api.dto.ErrorResponse;
import com.bnm.individuals_api.dto.LoginRequest;
import com.bnm.individuals_api.dto.RefreshTokenRequest;
import com.bnm.individuals_api.dto.SuccessAuthResponse;
import com.bnm.individuals_api.dto.UserRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIT extends KeycloakTestContainers {

  @Autowired
  private WebTestClient webTestClient;

  @Test
  public void givenValidIndividualRegistration_whenRegisterIndividual_then201Response() {

    // Given
    final UserRegistrationRequest validUserRegistration = new UserRegistrationRequest(
        "testEmail@mail.com",
        "testpassword", "testpassword");

    // When
    final WebTestClient.ResponseSpec result = webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(validUserRegistration), UserRegistrationRequest.class)
        .exchange();

    // Then
    result.expectStatus().isCreated();
  }

  @Test
  public void givenInvalidEmail_whenRegisterIndividual_then400Response() {
    // Given
    final UserRegistrationRequest invalidUserRegistration = new UserRegistrationRequest(
        "invalid-email",
        "pass", "pass");

    // When
    final WebTestClient.ResponseSpec result = webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(invalidUserRegistration), UserRegistrationRequest.class)
        .exchange();

    // Then
    result.expectStatus().isBadRequest().expectBody(ErrorResponse.class);
  }

  @Test
  public void givenInvalidPasswords_whenRegisterIndividual_then400Response() {
    // Given
    final UserRegistrationRequest invalidUserRegistration = new UserRegistrationRequest(
        "email@mail.com",
        "pass", "different-pass");

    // When
    final WebTestClient.ResponseSpec result = webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(invalidUserRegistration), UserRegistrationRequest.class)
        .exchange();

    // Then
    result.expectStatus().isBadRequest().expectBody(ErrorResponse.class);
  }

  @Test
  public void givenValidIndividualRegistration_whenRepeatRegisterIndividual_then409Response() {
    // Given
    final UserRegistrationRequest validUserRegistration = new UserRegistrationRequest(
        "duplicatedTest@mail.com",
        "testpassword", "testpassword");

    // When
    webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(validUserRegistration), UserRegistrationRequest.class)
        .exchange()
        .expectStatus().isCreated();

    final WebTestClient.ResponseSpec result = webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(validUserRegistration), ErrorResponse.class)
        .exchange();

    // Then
    result.expectStatus().isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  public void loginShouldReturn200() {
    // Given
    final UserRegistrationRequest userRegistration = new UserRegistrationRequest(
        "login2-test@mail.com",
        "testpassword", "testpassword");

    webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(userRegistration), UserRegistrationRequest.class)
        .exchange()
        .expectStatus().isCreated();

    // When
    final WebTestClient.ResponseSpec result = webTestClient.post().uri("/v1/auth/login")
        .body(Mono.just(new LoginRequest("login2-test@mail.com", "testpassword")),
            LoginRequest.class)
        .exchange();

    // Then
    result.expectStatus().isOk()
        .expectBody(SuccessAuthResponse.class);
  }

  @Test
  public void loginShouldReturn401() {
    // When
    final WebTestClient.ResponseSpec result = webTestClient.post().uri("/v1/auth/login")
        .body(Mono.just(new LoginRequest("wrong@mail.com", "wrongpassword")), LoginRequest.class)
        .exchange();

    // Then
    result.expectStatus().isUnauthorized();
  }

  @Test
  public void refreshToken200() {
    // Given
    final UserRegistrationRequest userRegistration = new UserRegistrationRequest(
        "ressfresh-test@mail.com",
        "testpassword", "testpassword");

    webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(userRegistration), UserRegistrationRequest.class)
        .exchange()
        .expectStatus().isCreated();

    final SuccessAuthResponse loginResponse = webTestClient.post().uri("/v1/auth/login")
        .body(Mono.just(new LoginRequest("ressfresh-test@mail.com", "testpassword")),
            LoginRequest.class)
        .exchange()
        .expectStatus().isOk()
        .returnResult(SuccessAuthResponse.class)
        .getResponseBody()
        .blockFirst();

    // When
    final WebTestClient.ResponseSpec refreshedInfo = webTestClient.post()
        .uri("/v1/auth/refresh-token")
        .body(Mono.just(new RefreshTokenRequest(loginResponse.refreshToken())),
            RefreshTokenRequest.class)
        .exchange();

    // Then
    refreshedInfo.expectStatus().isOk()
        .expectBody(SuccessAuthResponse.class);
  }

  @Test
  public void refreshToken401() {
    // When
    final WebTestClient.ResponseSpec refreshedInfo = webTestClient.post()
        .uri("/v1/auth/refresh-token")
        .body(Mono.just(new RefreshTokenRequest("invalid-refresh-token")),
            RefreshTokenRequest.class)
        .exchange();

    // Then
    refreshedInfo.expectStatus().isUnauthorized();
  }

  @Test
  public void aboutMe200() {
    // Given
    final UserRegistrationRequest userRegistration = new UserRegistrationRequest(
        "aabout-me-test@mail.com",
        "testpassword", "testpassword");

    webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(userRegistration), UserRegistrationRequest.class)
        .exchange()
        .expectStatus().isCreated();

    final SuccessAuthResponse loginResponse = webTestClient.post().uri("/v1/auth/login")
        .body(Mono.just(new LoginRequest("aabout-me-test@mail.com", "testpassword")),
            LoginRequest.class)
        .exchange()
        .expectStatus().isOk()
        .returnResult(SuccessAuthResponse.class)
        .getResponseBody()
        .blockFirst();

    // When
    final WebTestClient.ResponseSpec result = webTestClient.get().uri("/v1/auth/me")
        .header("Authorization", "Bearer " + loginResponse.accessToken())
        .exchange();

    // Then
    result.expectStatus().isOk()
        .expectBody(AboutMeResponse.class);
  }

  @Test
  public void aboutMe401() {
    // When
    final WebTestClient.ResponseSpec result = webTestClient.get().uri("/v1/auth/me")
        .header("Authorization", "Bearer invalid-token")
        .exchange();

    // Then
    result.expectStatus().isUnauthorized();
  }
}