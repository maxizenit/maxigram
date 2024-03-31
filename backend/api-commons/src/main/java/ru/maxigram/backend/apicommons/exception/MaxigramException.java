package ru.maxigram.backend.apicommons.exception;

public class MaxigramException extends RuntimeException {

  private final int httpStatusCode;

  public MaxigramException(String message, int httpStatusCode) {
    super(message);
    this.httpStatusCode = httpStatusCode;
  }

  public int getHttpStatusCode() {
    return httpStatusCode;
  }
}
