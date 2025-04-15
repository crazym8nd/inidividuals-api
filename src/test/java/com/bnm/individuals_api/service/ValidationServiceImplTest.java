package com.bnm.individuals_api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bnm.individuals_api.exception.InvalidRefreshToken;
import com.bnm.individuals_api.exception.InvalidRequestDataException;
import com.bnm.individuals_api.model.RefreshToken;
import com.bnm.individuals_api.model.UserRegistration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidationServiceImplTest {

    @InjectMocks
    private ValidationServiceImpl validationService;

    @Test
    void shouldValidateCorrectEmail() {
        // given
        String validEmail = "test@example.com";
        // when & then
        assertDoesNotThrow(() -> validationService.validateEmail(validEmail));
    }

    @Test
    void shouldThrowExceptionForEmptyEmail() {
        // given
        String emptyEmail = "";
        // when & then
        assertThrows(InvalidRequestDataException.class,
            () -> validationService.validateEmail(emptyEmail),
            "Email не может быть пустым");
    }

    @Test
    void shouldThrowExceptionForInvalidEmailFormat() {
        // given
        String invalidEmail = "invalid-email";
        // when & then
        assertThrows(InvalidRequestDataException.class,
            () -> validationService.validateEmail(invalidEmail),
            "Неверный формат email адреса");
    }

    @Test
    void shouldValidateCorrectPassword() {
        // given
        String password = "password123";
        String confirmPassword = "password123";
        // when & then
        assertDoesNotThrow(() -> validationService.validatePassword(password, confirmPassword));
    }

    @Test
    void shouldThrowExceptionForEmptyPassword() {
        // given
        String password = "";
        String confirmPassword = "";
        // when & then
        assertThrows(InvalidRequestDataException.class,
            () -> validationService.validatePassword(password, confirmPassword),
            "Пароль не может быть пустым");
    }

    @Test
    void shouldThrowExceptionForNonMatchingPasswords() {
        // given
        String password = "password123";
        String confirmPassword = "password124";
        // when & then
        assertThrows(InvalidRequestDataException.class,
            () -> validationService.validatePassword(password, confirmPassword),
            "Пароли не совпадают");
    }

    @Test
    void shouldValidateCorrectUserRegistration() {
        // given
        UserRegistration validRegistration = new UserRegistration(
            "test@example.com",
            "password123",
            "password123"
        );
        // when & then
        assertDoesNotThrow(() -> validationService.validateUserRegistration(validRegistration));
    }

    @Test
    void shouldReturnTrueForValidUserRegistration() {
        // given
        UserRegistration validRegistration = new UserRegistration(
            "test@example.com",
            "password123",
            "password123"
        );
        // when & then
        assertTrue(validationService.isValidUserRegistration(validRegistration));
    }

    @Test
    void shouldReturnFalseForInvalidUserRegistration() {
        // given
        UserRegistration invalidRegistration = new UserRegistration(
            "invalid-email",
            "password123",
            "password123"
        );
        // when & then
        assertFalse(validationService.isValidUserRegistration(invalidRegistration));
    }

    @Test
    void shouldValidateCorrectRefreshToken() {
        // given
        RefreshToken validToken = new RefreshToken("valid-token");
        // when & then
        assertDoesNotThrow(() -> validationService.validateRefreshToken(validToken));
    }

    @Test
    void shouldThrowExceptionForNullRefreshToken() {
        // given
        RefreshToken nullToken = null;
        // when & then
        assertThrows(InvalidRefreshToken.class,
            () -> validationService.validateRefreshToken(nullToken),
            "Invalid refresh token");
    }

    @Test
    void shouldThrowExceptionForEmptyRefreshToken() {
        // given
        RefreshToken emptyToken = new RefreshToken("");
        // when & then
        assertThrows(InvalidRefreshToken.class,
            () -> validationService.validateRefreshToken(emptyToken),
            "Invalid refresh token");
    }
} 