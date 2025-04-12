package com.bnm.individuals_api.api;

import com.bnm.individuals_api.dto.AboutMeResponse;
import com.bnm.individuals_api.dto.ErrorResponse;
import com.bnm.individuals_api.dto.LoginRequest;
import com.bnm.individuals_api.dto.RefreshTokenRequest;
import com.bnm.individuals_api.dto.SuccessAuthResponse;
import com.bnm.individuals_api.dto.UserRegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

@Tag(name = "API для auth")
public interface AuthApi {

  @Operation(summary = "Регистрация пользователя")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "201", description = "Успешная регистрация",
          content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = SuccessAuthResponse.class)
              )
          }
      ),
      @ApiResponse(
          responseCode = "400", description = "Ошибка валидации запроса",
          content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponse.class)
              )
          }
      ),
      @ApiResponse(
          responseCode = "409", description = "Конфликт -- пользователь с таким email уже существует",
          content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponse.class)
              )
          }
      )
  })
  Mono<SuccessAuthResponse> registerUser(UserRegistrationRequest request);

  @Operation(summary = "Аутентификация пользователя")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200", description = "Успешная аутентификация",
          content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = SuccessAuthResponse.class)
              )
          }
      ),
      @ApiResponse(
          responseCode = "401", description = "Ошибка аутентификации",
          content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponse.class)
              )
          }
      )
  })
  Mono<SuccessAuthResponse> loginUser(LoginRequest request);

  @Operation(summary = "Получение информации о пользователе")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200", description = "Информация о пользователе",
          content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = AboutMeResponse.class)
              )
          }
      ),
      @ApiResponse(
          responseCode = "401", description = "Отсутствует аутентификация",
          content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponse.class)
              )
          }
      )
  })
  Mono<AboutMeResponse> aboutMe(Principal principal);

  @Operation(summary = "Обновление токена доступа")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200", description = "Обновленные данные аутентификации",
          content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = SuccessAuthResponse.class)
              )
          }
      ),
      @ApiResponse(
          responseCode = "401", description = "Отсутствует аутентификация",
          content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponse.class)
              )
          }
      )
  })
  Mono<SuccessAuthResponse> refreshToken(RefreshTokenRequest request);
}
