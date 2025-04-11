package com.bnm.individuals_api.api;

import com.bnm.individuals_api.KeycloakTestContainer;
import com.bnm.individuals_api.dto.AboutMeResponse;
import com.bnm.individuals_api.dto.RefreshTokenRequest;
import com.bnm.individuals_api.dto.SuccessUserRegistration;
import com.bnm.individuals_api.dto.UserRegistration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.main.allow-bean-definition-overriding=true")
@Testcontainers
@TestPropertySource(locations = "classpath:application-test.yaml")
class AuthControllerIT extends KeycloakTestContainer {

  @Autowired
  private WebTestClient webTestClient;

  @Test
  public void givenValidIndividualRegistration_whenRegisterIndividual_then201Response() {

    // Given
    final UserRegistration validUserRegistration = new UserRegistration("testEmail@mail.com",
        "testpassword", "testpassword");

    // When
    final WebTestClient.ResponseSpec result = webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(validUserRegistration), UserRegistration.class)
        .exchange();

    // Then
    result.expectStatus().isCreated();
  }

  @Test
  public void givenInValidIndividualRegistration_whenRegisterIndividual_then400Response() {

    // Given
    final UserRegistration validUserRegistration = new UserRegistration("",
        "testpassword", "testpassword");

    // When
    final WebTestClient.ResponseSpec result = webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(validUserRegistration), UserRegistration.class)
        .exchange();

    // Then
    result.expectStatus().isBadRequest();
  }

  @Test
  public void givenValidIndividualRegistration_whenRepeatRegisterIndividual_then409Response() {

    final UserRegistration validUserRegistration = new UserRegistration("",
        "testpassword", "testpassword");
    final WebTestClient.ResponseSpec registration = webTestClient.post()
        .uri("/v1/auth/registration")
        .body(Mono.just(validUserRegistration), UserRegistration.class)
        .exchange();
    registration.expectStatus().isCreated();

    final WebTestClient.ResponseSpec result = webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(validUserRegistration), UserRegistration.class)
        .exchange();

    result.expectStatus().isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  public void loginShouldReturn200() {
    // Given
    final UserRegistration validUserRegistration = new UserRegistration("testEmail@mail.com",
        "testpassword", "testpassword");
    final WebTestClient.ResponseSpec register = webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(validUserRegistration), UserRegistration.class)
        .exchange();
    final var validLoginRequest = validUserRegistration;

    // When
    final WebTestClient.ResponseSpec result = webTestClient.post().uri("/v1/auth/login")
        .body(Mono.just(validLoginRequest), UserRegistration.class)
        .exchange();

    // Then
    result.expectStatus().isOk();
  }

  @Test
  public void loginShouldReturn401() {
    // Given
    final UserRegistration validUserRegistration = new UserRegistration("testEmail@mail.com",
        "testpassword", "testpassword");
    final WebTestClient.ResponseSpec register = webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(validUserRegistration), UserRegistration.class)
        .exchange();

    final var validLoginRequest = new UserRegistration(validUserRegistration.email(),
        "", "");

    // When
    final WebTestClient.ResponseSpec result = webTestClient.post().uri("/v1/auth/login")
        .body(Mono.just(validLoginRequest), UserRegistration.class)
        .exchange();

    // Then
    result.expectStatus().isUnauthorized();
  }

  @Test
  public void refreshToken200() {

    // Given
    final UserRegistration validUserRegistration = new UserRegistration("testEmail@mail.com",
        "testpassword", "testpassword");
    final WebTestClient.ResponseSpec register = webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(validUserRegistration), UserRegistration.class)
        .exchange();
    final var validLoginRequest = validUserRegistration;

    // When
    final WebTestClient.ResponseSpec result = webTestClient.post().uri("/v1/auth/login")
        .body(Mono.just(validLoginRequest), UserRegistration.class)
        .exchange();

    // Then
    final var loginResponse = result.expectStatus().isOk()
        .expectBody(SuccessUserRegistration.class)
        .returnResult().getResponseBody();

    final WebTestClient.ResponseSpec refreshedInfo = webTestClient.post().uri("/v1/auth/refresh-token")
        .body(Mono.just(loginResponse), RefreshTokenRequest.class)
        .exchange();

    refreshedInfo.expectStatus().isOk();
  }

  @Test
  public void refreshToken401() {

    final WebTestClient.ResponseSpec refreshedInfo = webTestClient.post().uri("/v1/auth/refresh-token")
        .body(Mono.just("brtgbfrbfgtbtrb"), RefreshTokenRequest.class)
        .exchange();

    refreshedInfo.expectStatus().isUnauthorized();
  }

  @Test
  public void aboutMe200() {
    final UserRegistration validUserRegistration = new UserRegistration("testEmail@mail.com",
        "testpassword", "testpassword");
    final WebTestClient.ResponseSpec register = webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(validUserRegistration), UserRegistration.class)
        .exchange();
    final UserRegistration validLoginRequest = validUserRegistration;
    final WebTestClient.ResponseSpec response = webTestClient.post().uri("/v1/auth/login")
        .body(Mono.just(validLoginRequest), UserRegistration.class)
        .exchange();

    final SuccessUserRegistration token = response
        .expectStatus().isOk()
        .expectBody(SuccessUserRegistration.class)
        .returnResult().getResponseBody();

    final String tokenValue = token.accessToken();

    // When
    final WebTestClient.ResponseSpec result = webTestClient.get().uri("/v1/auth/me")
        .headers(headers -> headers.setBearerAuth(tokenValue))
        .exchange();

    // Then
    result.expectStatus().isOk()
        .expectBody(AboutMeResponse.class);
  }

  @Test
  public void aboutMe401() {
    final String tokenValue = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJQdnVrMm5VYjF6RER0NEZ3QjIzVm1yU3VmRVQtd1E3VWN2RWtsU0ppV1FZIn0.eyJleHAiOjE3NDQzODA3NDEsImlhdCI6MTc0NDM4MDQ0MSwianRpIjoiODcxZjIzMWQtMzQ0ZC00MmFiLWJlY2ItOWNiNmQ5ZmZlYzIzIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo5MDA1L3JlYWxtcy9hcHBhdXRoIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6ImFlYjZjNzQwLWZiOTQtNGY4Yi1iM2Y2LWUyYjMyZjQ4YWZlYiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImFwcC1hdXRoLWNsaWVudC1pZCIsInNlc3Npb25fc3RhdGUiOiIzMTAwNDE3NC05YjVhLTQ5MDctYTk1MC0yNTA3ZTBkNzYxZWIiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly9sb2NhbGhvc3Q6NjY2Ni8qIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJJTkRJVklEVUFMUyIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLWFwcGF1dGgiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJzaWQiOiIzMTAwNDE3NC05YjVhLTQ5MDctYTk1MC0yNTA3ZTBkNzYxZWIiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6ImZpcnN0TmFtZSBsYXN0TmFtZSIsInByZWZlcnJlZF91c2VybmFtZSI6IjExMUBhc2QuY29tIiwiZ2l2ZW5fbmFtZSI6ImZpcnN0TmFtZSIsImZhbWlseV9uYW1lIjoibGFzdE5hbWUiLCJlbWFpbCI6IjExMUBhc2QuY29tIn0.MPwPUhliZFHARDT4gfJ7tV3YMr-JrGcVXBtWUS8xnJbRuYqSxvb3CZ_rsWbOyWY1wS1SrJuDbtQBAM_ZCF1N4NDIVC6Kf8e0GZN8MMuU8PK3gmU5VhJV3wMmWHRCk2DDCzFQKL4MQsluRmmlHIiFAIVDeZ99tO8H_sug5fNQPPLz6SI1gJHKQNse25MaJA_4psr6abcmueLOK_qFrC6fMg_5W2BycjoKljqdGJ5pJzPSg9XerIT3uDBM12-jEx3mFdME6r6esldZ6T_jIZOsDx5mzu-d0a1FXnarbf4_lQiKLcdQTdPhnKDMBb_GkD0oSCydLh1DdqZs1wm0bD1GdQ";

    // When
    final WebTestClient.ResponseSpec result = webTestClient.get().uri("/v1/auth/me")
        .headers(headers -> headers.setBearerAuth(tokenValue))
        .exchange();

    // Then
    result.expectStatus().isUnauthorized();
  }
}