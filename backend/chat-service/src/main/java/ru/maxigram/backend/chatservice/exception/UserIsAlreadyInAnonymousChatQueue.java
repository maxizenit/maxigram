package ru.maxigram.backend.chatservice.exception;

import ru.maxigram.backend.apicommons.exception.MaxigramException;

/** Сигнализирует о том, что пользователь уже подал заявку на поиск анонимного собеседника. */
public class UserIsAlreadyInAnonymousChatQueue extends MaxigramException {

  private static final String MESSAGE = "User with id '%s' is already in anonymous chat queue";

  public UserIsAlreadyInAnonymousChatQueue(String userId) {
    super(String.format(MESSAGE, userId), 304);
  }
}
