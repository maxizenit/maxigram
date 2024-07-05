package ru.maxizenit.backend.chatservice.exception;

import ru.maxigram.backend.maxigramcommons.exception.MaxigramException;

public class ChatIsNotAnonymousException extends MaxigramException {

  private static final String MESSAGE = "Chat with id '%d' is not anonymous";

  public ChatIsNotAnonymousException(long chatId) {
    super(String.format(MESSAGE, chatId), 400);
  }
}
