package ru.maxigram.backend.chatservice.exception;

import ru.maxigram.backend.apicommons.exception.MaxigramException;

/** Сигнализирует о том, что чат не найден. */
public class ChatNotFoundException extends MaxigramException {

  private static final String MESSAGE = "Chat with id '%d' not found";

  public ChatNotFoundException(Long chatId) {
    super(String.format(MESSAGE, chatId), 404);
  }
}
