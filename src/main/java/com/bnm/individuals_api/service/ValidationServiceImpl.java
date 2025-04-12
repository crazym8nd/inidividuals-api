package com.bnm.individuals_api.service;

import com.bnm.individuals_api.exception.InvalidRequestData;
import com.bnm.individuals_api.model.UserRegistration;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ValidationServiceImpl implements ValidationService {

  private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
  );

  @Override
  public void validateEmail(final String email) {
    if (StringUtils.isBlank(email)) {
      throw new InvalidRequestData("Email не может быть пустым");
    }
    if (!EMAIL_PATTERN.matcher(email).matches()) {
      throw new InvalidRequestData("Неверный формат email адреса");
    }
  }

  @Override
  public void validatePassword(final String password, final String confirmPassword) {
    if (StringUtils.isBlank(password)) {
      throw new InvalidRequestData("Пароль не может быть пустым");
    }
    if (!StringUtils.equals(password, confirmPassword)) {
      throw new InvalidRequestData("Пароли не совпадают");
    }
  }

  @Override
  public boolean isValid(final UserRegistration userRegistration) {
    try {
      validateEmail(userRegistration.email());
      validatePassword(userRegistration.password(), userRegistration.confirmPassword());
      return true;
    } catch (final InvalidRequestData e) {
      return false;
    }
  }
}
