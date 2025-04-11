package com.bnm.individuals_api.api;

import com.bnm.individuals_api.dto.AboutMeResponse;
import com.bnm.individuals_api.dto.ErrorResponse;
import com.bnm.individuals_api.dto.RefreshTokenRequest;
import com.bnm.individuals_api.dto.SuccessUserRegistration;
import com.bnm.individuals_api.dto.UserRegistrationRequest;
import com.bnm.individuals_api.dto.UserRegistrationResponse;
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

  UserRegistrationResponse testEndpoint(UserRegistrationRequest request);

  @Operation(summary = "Регистрация пользователя")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "201", description = "Успешная регистрация",
          content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = UserRegistrationResponse.class)
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
                  mediaType = MediaType.APPLICATION_JSON_VALUE
              )
          }
      ),
      @ApiResponse(responseCode = "500", content = @Content)
  })
  Mono<UserRegistrationResponse> registerUser(UserRegistrationRequest request);

  Mono<SuccessUserRegistration> loginUser(UserRegistrationRequest request);

  Mono<AboutMeResponse> aboutMe(Principal principal);

  Mono<SuccessUserRegistration> refreshToken(RefreshTokenRequest request);
}
