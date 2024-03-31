package ru.maxigram.backend.chatservice.exception;

import ru.maxigram.backend.apicommons.exception.MaxigramException;

/** Сигнализирует о том, что закрываемый чат уже закрыт. */
public class ChatIsAlreadyClosedException extends MaxigramException {

  private static final String MESSAGE = "Chat with id '%d' is already closed";

  public ChatIsAlreadyClosedException(Long chatId) {
    super(String.format(MESSAGE, chatId), 304);
  }
}
