package ru.maxigram.backend.maxigramcommons.rabbit.dto;

import java.io.Serializable;
import java.util.Collection;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserActionsFromFeedServiceResult implements Serializable {

  private String userId;
  private String firstUserId;
  private String secondUserId;
  private Collection<Long> commentedPostIds;
  private Collection<Long> likedPostIds;
  private Collection<Long> likedCommentsIds;
}
