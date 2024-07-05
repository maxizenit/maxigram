package ru.maxizenit.backend.chatservice.exception;

import ru.maxigram.backend.maxigramcommons.exception.MaxigramException;

public class CommentNotFoundException extends MaxigramException {

  private static final String MESSAGE = "Comment with id '%d' not found";

  public CommentNotFoundException(long id) {
    super(String.format(MESSAGE, id), 404);
  }
}
