package com.bnm.individuals_api.service;

import com.bnm.individuals_api.exception.InvalidRequestDataException;
import com.bnm.individuals_api.model.RefreshToken;
import com.bnm.individuals_api.model.UserRegistration;

/**
 * Сервис валидации данных пользователя.
 */
public interface ValidationService {

  /**
   * Проверяет корректность email адреса.
   *
   * @param email email адрес для проверки
   * @throws InvalidRequestDataException если email не соответствует требованиям
   */
  void validateEmail(String email);

  /**
   * Проверяет корректность пароля и его подтверждения.
   *
   * @param password        пароль для проверки
   * @param confirmPassword подтверждение пароля
   * @throws InvalidRequestDataException если пароль не совпадает с подтверждением
   */
  void validatePassword(String password, String confirmPassword);

  /**
   * Выполняет полную валидацию данных регистрации пользователя.
   *
   * @param userRegistration данные регистрации пользователя
   * @throws InvalidRequestDataException если данные не соответствуют требованиям
   */
  void validateUserRegistration(UserRegistration userRegistration);

  /**
   * Проверяет валидность данных регистрации пользователя без выбрасывания исключений.
   *
   * @param userRegistration данные регистрации пользователя
   * @return true если данные валидны, false в противном случае
   */
  boolean isValidUserRegistration(UserRegistration userRegistration);

  void validateRefreshToken(RefreshToken request);
}
