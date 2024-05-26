package ru.maxigram.backend.maxigramcommons.dto;

import java.sql.Timestamp;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class SelfRestraint {

  private String userId;
  private Timestamp startTime;
  private Timestamp endTime;
}
