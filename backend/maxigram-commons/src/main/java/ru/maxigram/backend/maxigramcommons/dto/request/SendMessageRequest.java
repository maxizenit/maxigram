package ru.maxigram.backend.maxigramcommons.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class SendMessageRequest {

  private String senderId;
  private String text;
}
