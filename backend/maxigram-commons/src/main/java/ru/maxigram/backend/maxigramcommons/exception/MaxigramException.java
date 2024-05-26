package ru.maxigram.backend.maxigramcommons.exception;

import lombok.Getter;

public class MaxigramException extends RuntimeException {

  @Getter private final int httpStatusCode;

  public MaxigramException(String message, int httpStatusCode) {
    super(message);
    this.httpStatusCode = httpStatusCode;
  }
}
