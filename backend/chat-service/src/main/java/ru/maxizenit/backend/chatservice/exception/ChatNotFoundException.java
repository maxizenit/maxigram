package ru.maxizenit.backend.chatservice.exception;

import ru.maxigram.backend.maxigramcommons.exception.MaxigramException;

public class ChatNotFoundException extends MaxigramException {

  private static final String MESSAGE = "Chat with id '%d' not found";

  public ChatNotFoundException(long chatId) {
    super(String.format(MESSAGE, chatId), 404);
  }
}
