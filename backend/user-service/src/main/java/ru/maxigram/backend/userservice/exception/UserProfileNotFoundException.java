package ru.maxigram.backend.userservice.exception;

import ru.maxigram.backend.apicommons.exception.MaxigramException;

public class UserProfileNotFoundException extends MaxigramException {

  private static final String MESSAGE = "User profile with id '%s' not found";

  public UserProfileNotFoundException(String userProfileId) {
    super(String.format(MESSAGE, userProfileId), 404);
  }
}
