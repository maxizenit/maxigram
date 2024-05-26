package ru.maxizenit.backend.chatservice.exception;

import ru.maxigram.backend.maxigramcommons.exception.MaxigramException;

public class UserProfileNotFoundException extends MaxigramException {

  private static final String MESSAGE = "User profile with id '%s' not found";

  public UserProfileNotFoundException(String id) {
    super(String.format(MESSAGE, id), 404);
  }
}
