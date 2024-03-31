package ru.maxigram.backend.chatservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

/** Сообщение. */
@Entity
@Getter
@Setter
public class Message {

  /** Идентификатор. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Чат. */
  @ManyToOne
  @JoinColumn(name = "chat_id")
  private Chat chat;

  /** Время отправки. */
  private Timestamp timestamp;

  /** Идентификатор отправителя. */
  private String senderId;

  /** Текст. */
  private String text;

  /** Флаг прочтения. */
  private Boolean read;
}
