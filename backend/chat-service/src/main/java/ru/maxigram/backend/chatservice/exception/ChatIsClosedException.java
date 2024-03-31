package ru.maxigram.backend.chatservice.exception;

import ru.maxigram.backend.apicommons.exception.MaxigramException;

/** Сигнализирует о том, что чат закрыт. */
public class ChatIsClosedException extends MaxigramException {

  private static final String MESSAGE = "Chat with id '%d' is closed";

  public ChatIsClosedException(Long chatId) {
    super(String.format(MESSAGE, chatId), 403);
  }
}
