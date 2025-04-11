package com.bnm.individuals_api.api;

import com.bnm.individuals_api.KeycloakTestContainer;
import com.bnm.individuals_api.dto.UserRegistration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
  @DisplayName("Should return 201 response fore creation of individual")
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
}