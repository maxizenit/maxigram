package ru.maxigram.backend.feedservice.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;

/** Пост. */
@Entity
@Getter
@Setter
public class Post {

  /** Идентификатор. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Идентификатор автора. */
  private String authorId;

  /** Текст. */
  private String text;

  /** Время написания. */
  private Timestamp timestamp;

  /** Комментарии. */
  @OneToMany(mappedBy = "post", fetch = FetchType.EAGER)
  private Collection<Comment> comments;

  /** Лайки. */
  @OneToMany(mappedBy = "post", fetch = FetchType.EAGER)
  private Collection<Like> likes;
}
