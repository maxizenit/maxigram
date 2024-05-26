package ru.maxizenit.backend.chatservice.exception;

import ru.maxigram.backend.maxigramcommons.exception.MaxigramException;

public class UserIsNotInChatException extends MaxigramException {

  private static final String MESSAGE = "User with id '%s' is not in chat with id '%d'";

  public UserIsNotInChatException(String userId, long chatId) {
    super(String.format(MESSAGE, userId, chatId), 403);
  }
}
