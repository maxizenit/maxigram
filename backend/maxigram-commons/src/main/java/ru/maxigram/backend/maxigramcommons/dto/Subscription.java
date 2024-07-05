package ru.maxigram.backend.maxigramcommons.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class Subscription {

  private String subscriberId;
  private String authorId;
}
