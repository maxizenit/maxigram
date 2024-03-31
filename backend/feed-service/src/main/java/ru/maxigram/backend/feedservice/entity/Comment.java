package ru.maxigram.backend.feedservice.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;

/** Комментарий. */
@Entity
@Getter
@Setter
public class Comment {

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

  /** Пост, к которому написан комментарий. */
  @ManyToOne
  @JoinColumn(name = "post_id")
  private Post post;

  /** Лайки. */
  @OneToMany(mappedBy = "comment", fetch = FetchType.EAGER)
  private Collection<Like> likes;
}
