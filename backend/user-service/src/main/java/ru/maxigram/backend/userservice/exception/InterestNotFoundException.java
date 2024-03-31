package ru.maxigram.backend.userservice.exception;

import ru.maxigram.backend.apicommons.exception.MaxigramException;

public class InterestNotFoundException extends MaxigramException {

  private static final String MESSAGE = "Interest with id '%d' not found";

  public InterestNotFoundException(Long interestId) {
    super(String.format(MESSAGE, interestId), 404);
  }
}
