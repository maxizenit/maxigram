package ru.maxigram.backend.chatservice.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;

/** Чат. */
@Entity
@Getter
@Setter
public class Chat {

  /** Идентификатор. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Идентификатор первого собеседника. */
  private String firstParticipantId;

  /** Идентификатор второго собеседника. */
  private String secondParticipantId;

  /** Флаг анонимности. */
  private Boolean anonymous;

  /** Флаг согласия первого пользователя на деанонимизацию. */
  private Boolean firstParticipantAgreesToDeanonymization;

  /** Флаг согласия второго пользователя на деанонимизацию. */
  private Boolean secondParticipantAgreesToDeanonymization;

  /** Флаг закрытия чата. Применяется для анонимных чатов. */
  private Boolean isClosed;

  /** Идентификатор пользователя, закрывшего чат. */
  private String participantClosedChatId;

  /**
   * Идентификатор нового чата (назначается в случае деанонимизации обоих участников анонимного
   * чата).
   */
  private Long newChatId;

  /** Время создания чата. */
  private Timestamp creationTimestamp;

  /** Сообщения чата. */
  @OneToMany(mappedBy = "chat", fetch = FetchType.EAGER)
  private Collection<Message> messages;
}
