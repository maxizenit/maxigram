package ru.maxizenit.backend.chatservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Chat {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String firstParticipantId;
  private String secondParticipantId;
  private Boolean anonymous;
  private Boolean firstParticipantAgreeToDeAnonymization;
  private Boolean secondParticipantAgreeToDeAnonymization;
  private Boolean isClosed;
  private Long newChatId;
}
