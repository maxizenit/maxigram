package ru.maxizenit.backend.chatservice.exception;

import ru.maxigram.backend.maxigramcommons.exception.MaxigramException;

public class ChatIsAlreadyClosedException extends MaxigramException {

  private static final String MESSAGE = "Chat with id '%d' is already closed";

  public ChatIsAlreadyClosedException(long chatId) {
    super(String.format(MESSAGE, chatId), 422);
  }
}
