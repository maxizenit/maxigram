package ru.maxigram.backend.apicommons.dto;

import java.sql.Timestamp;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class ChatListElement {

  private Long chatId;

  private String lastMessage;

  private Timestamp lastMessageTimestamp;

  private Boolean read;

  private String otherParticipantFirstName;

  private String otherParticipantLastName;
}
