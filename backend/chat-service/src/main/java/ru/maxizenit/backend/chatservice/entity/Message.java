package ru.maxizenit.backend.chatservice.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "chat_id")
  private Chat chat;

  private String senderId;
  private String text;
  private Timestamp timestamp;
  private Boolean read;
}
