package ru.maxigram.backend.feedservice.exception;

import ru.maxigram.backend.apicommons.exception.MaxigramException;

/** Сигнализирует о том, что комментарий не найден. */
public class CommentNotFoundException extends MaxigramException {

  private static final String MESSAGE = "Comment with id '%d' not found";

  public CommentNotFoundException(Long commentId) {
    super(String.format(MESSAGE, commentId), 404);
  }
}
