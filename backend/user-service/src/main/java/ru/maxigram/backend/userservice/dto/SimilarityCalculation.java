package ru.maxigram.backend.userservice.dto;

import java.util.Collection;
import lombok.Getter;
import lombok.Setter;

/** Расчёт сходства двух профилей. */
@Getter
@Setter
public class SimilarityCalculation {

  /** Идентификатор первого пользователя. */
  private String firstUserId;

  /** Идентификатор второго пользователя. */
  private String secondUserId;

  /** Сходство возраста (1 - разница_в_годах * 0.05). */
  private Double ageSimilarity;

  /** Сходство интересов. */
  private Double interestsSimilarity;

  /** Сходство действий в feed-service. */
  private Double feedActionsSimilarity;

  private Collection<Long> firstUserCommentedPostIds;

  private Collection<Long> firstUserLikedPostIds;

  private Collection<Long> firstUserLikedCommentsIds;

  private Collection<Long> secondUserCommentedPostIds;

  private Collection<Long> secondUserLikedPostIds;

  private Collection<Long> secondUserLikedCommentsIds;

  private Boolean firstUserFeedInformationCompleted = false;

  private Boolean secondUserFeedInformationCompleted = false;
}
