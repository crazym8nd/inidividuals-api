package com.bnm.individuals_api.api;

import com.bnm.individuals_api.KeycloakTestContainer;
import com.bnm.individuals_api.dto.UserRegistration;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
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
    final WebTestClient.ResponseSpec registration = webTestClient.post().uri("/v1/auth/registration")
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
    WebTestClient.ResponseSpec register = webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(validUserRegistration), UserRegistration.class)
        .exchange();
    final var validLoginRequest = validUserRegistration;

    // When
    WebTestClient.ResponseSpec result = webTestClient.post().uri("/v1/auth/login")
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
    WebTestClient.ResponseSpec register = webTestClient.post().uri("/v1/auth/registration")
        .body(Mono.just(validUserRegistration), UserRegistration.class)
        .exchange();

    final var validLoginRequest = new UserRegistration(validUserRegistration.email(),
        "", "");

    // When
    WebTestClient.ResponseSpec result = webTestClient.post().uri("/v1/auth/login")
        .body(Mono.just(validLoginRequest), UserRegistration.class)
        .exchange();

    // Then
    result.expectStatus().isUnauthorized();
  }

  @Test
  public void refreshToken200() {

    Assertions.assertTrue(false);
  }

  @Test
  public void refreshToken401() {

    Assertions.assertTrue(false);
  }

  @Test
  public void aboutMe200() {

    Assertions.assertTrue(false);
  }

  @Test
  public void aboutMe404() {

    Assertions.assertTrue(false);
  }

  @Test
  public void aboutMe401() {

    Assertions.assertTrue(false);
  }
}