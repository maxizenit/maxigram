package ru.maxizenit.backend.chatservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SelfRestraint {

  @Id private String userId;

  @OneToOne
  @MapsId
  @JoinColumn(name = "user_id")
  private UserProfile userProfile;
}
