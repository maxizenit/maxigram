package ru.maxizenit.backend.chatservice.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String authorId;
  private String text;
  private Timestamp timestamp;

  @OneToMany(mappedBy = "post")
  private Collection<Like> likes;

  @OneToMany(mappedBy = "post")
  private Collection<Comment> comments;
}
