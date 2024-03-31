package ru.maxigram.backend.apicommons.dto;

import java.sql.Timestamp;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class Message {

  private Long id;

  private Long chatId;

  private Timestamp timestamp;

  private String senderId;

  private String text;

  private Boolean read;
}
