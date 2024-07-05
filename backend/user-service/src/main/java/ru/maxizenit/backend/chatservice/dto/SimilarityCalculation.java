package ru.maxizenit.backend.chatservice.dto;

import java.util.Collection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimilarityCalculation {
  private String firstUserId;
  private String secondUserId;
  private Double ageSimilarity;
  private Double interestsSimilarity;
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
