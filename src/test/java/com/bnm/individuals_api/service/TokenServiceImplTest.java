package com.bnm.individuals_api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.bnm.individuals_api.exception.InvalidRefreshToken;
import com.bnm.individuals_api.model.AuthData;
import com.bnm.individuals_api.model.RefreshToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

  @InjectMocks
  private TokenServiceImpl tokenService;
  @Mock
  private KeycloakIntegrationService keycloakIntegrationService;
  @Mock
  private ValidationService validationService;

  @Test
  void shouldRefreshAccessToken() {
    // given
    RefreshToken refreshToken = new RefreshToken("test-refresh-token");
    AuthData authData = new AuthData("new-access-token", 300, "new-refresh-token", "Bearer");
    given(keycloakIntegrationService.refreshAccessToken(refreshToken))
        .willReturn(Mono.just(authData));
    // when
    final var result = tokenService.refreshAccessToken(refreshToken);
    // then
    StepVerifier.create(result)
        .expectNext(authData)
        .verifyComplete();
    verify(validationService).validateRefreshToken(refreshToken);
    verify(keycloakIntegrationService).refreshAccessToken(refreshToken);
  }

  @Test
  void shouldThrowInvalidRefreshTokenWhenValidationFails() {
    // given
    RefreshToken refreshToken = new RefreshToken("invalid-refresh-token");
    String errorMessage = "Invalid refresh token";
    doThrow(new InvalidRefreshToken(errorMessage))
        .when(validationService)
        .validateRefreshToken(refreshToken);

    assertThrows(InvalidRefreshToken.class, () -> {
      tokenService.refreshAccessToken(refreshToken).block();
    });

    verify(validationService).validateRefreshToken(refreshToken);
    verifyNoInteractions(keycloakIntegrationService);
  }

  @Test
  void shouldThrowInvalidRefreshTokenWhenKeycloakFails() {
    // given
    RefreshToken refreshToken = new RefreshToken("expired-token");
    String errorMessage = "Invalid refreshToken";
    given(keycloakIntegrationService.refreshAccessToken(refreshToken))
        .willReturn(Mono.error(new InvalidRefreshToken(errorMessage)));
    // when
    final var result = tokenService.refreshAccessToken(refreshToken);
    // then
    StepVerifier.create(result)
        .expectErrorMatches(throwable ->
            throwable instanceof InvalidRefreshToken &&
                throwable.getMessage().equals(errorMessage))
        .verify();
    verify(validationService).validateRefreshToken(refreshToken);
    verify(keycloakIntegrationService).refreshAccessToken(refreshToken);
  }
}