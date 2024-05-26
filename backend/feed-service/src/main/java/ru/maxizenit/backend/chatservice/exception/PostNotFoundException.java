package ru.maxizenit.backend.chatservice.exception;

import ru.maxigram.backend.maxigramcommons.exception.MaxigramException;

public class PostNotFoundException extends MaxigramException {

  private static final String MESSAGE = "Post with id '%d' not found";

  public PostNotFoundException(long id) {
    super(String.format(MESSAGE, id), 404);
  }
}
