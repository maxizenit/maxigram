package ru.maxigram.backend.feedservice.exception;

import ru.maxigram.backend.apicommons.exception.MaxigramException;

/** Сигнализирует о том, что пост не найден. */
public class PostNotFoundException extends MaxigramException {

  private static final String MESSAGE = "Post with id '%d' not found";

  public PostNotFoundException(Long postId) {
    super(String.format(MESSAGE, postId), 404);
  }
}
