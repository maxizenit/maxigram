package ru.maxizenit.backend.chatservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Subscription {

  @Id
  @Column(name = "subscriber_id")
  private String subscriberId;

  @Id
  @Column(name = "author_id")
  private String authorId;

  @ManyToOne
  @JoinColumn(name = "subscriber_id")
  private UserProfile subscriber;
}
