package ru.maxigram.backend.maxigramcommons.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class Chat {

  private Long id;
  private String firstParticipantId;
  private String secondParticipantId;
  private Boolean anonymous;
  private Boolean firstParticipantAgreeToDeAnonymization;
  private Boolean secondParticipantAgreeToDeAnonymization;
  private Boolean isClosed;
  private Long newChatId;
  private String lastMessage;
}
