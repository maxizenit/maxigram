package ru.maxigram.backend.chatservice.exception;

import ru.maxigram.backend.apicommons.exception.MaxigramException;

/** Сигнализирует о том, что пользователя нет в чате. */
public class UserIsNotInChatException extends MaxigramException {

  private static final String MESSAGE = "User with id '%s' is not in chat with id '%d'";

  public UserIsNotInChatException(String userId, Long chatId) {
    super(String.format(MESSAGE, userId, chatId), 403);
  }
}
