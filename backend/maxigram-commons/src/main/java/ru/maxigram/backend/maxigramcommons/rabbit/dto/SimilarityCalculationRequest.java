package ru.maxigram.backend.maxigramcommons.rabbit.dto;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimilarityCalculationRequest implements Serializable {

  String firstUserId;
  String secondUserId;
}
