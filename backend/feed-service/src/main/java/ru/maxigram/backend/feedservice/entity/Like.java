package ru.maxigram.backend.feedservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Лайк. */
@Entity
@Getter
@Setter
@Table(name = "like_")
public class Like {

  /** Идентификатор. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Идентификатор автора. */
  private String authorId;

  /** Пост, к которому поставлен лайк. */
  @ManyToOne
  @JoinColumn(name = "post_id")
  private Post post;

  /** Комментарий, к которому поставлен лайк. */
  @ManyToOne
  @JoinColumn(name = "comment_id")
  private Comment comment;
}
