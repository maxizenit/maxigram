package ru.maxigram.backend.maxigramcommons.dto;

import java.sql.Timestamp;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class Comment {

  private Long id;
  private Long postId;
  private String authorId;
  private String text;
  private Timestamp timestamp;
  private Long likesCount;
  private Boolean isLikedByRequester;
}
