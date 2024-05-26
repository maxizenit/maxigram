package ru.maxizenit.backend.chatservice.exception;

import ru.maxigram.backend.maxigramcommons.exception.MaxigramException;

public class UserIsAlreadyInAnonymousChatQueue extends MaxigramException {

  private static final String MESSAGE = "User with id '%s' is already in anonymous chat queue";

  public UserIsAlreadyInAnonymousChatQueue(String userId) {
    super(String.format(MESSAGE, userId), 304);
  }
}
