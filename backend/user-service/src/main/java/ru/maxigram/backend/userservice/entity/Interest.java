package ru.maxigram.backend.userservice.entity;

import jakarta.persistence.*;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;

/** Интерес. */
@Entity
@Getter
@Setter
public class Interest {

  /** Идентификатор. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Название. */
  private String name;

  @ManyToMany(mappedBy = "interests", fetch = FetchType.EAGER)
  private Collection<UserProfile> userProfiles;
}
