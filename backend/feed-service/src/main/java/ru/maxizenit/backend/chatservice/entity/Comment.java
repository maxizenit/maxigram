package ru.maxizenit.backend.chatservice.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "post_id")
  private Post post;

  private String authorId;
  private String text;
  private Timestamp timestamp;

  @OneToMany(mappedBy = "comment")
  private Collection<Like> likes;
}
