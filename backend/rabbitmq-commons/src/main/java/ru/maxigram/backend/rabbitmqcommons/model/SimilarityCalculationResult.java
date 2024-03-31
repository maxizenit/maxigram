package ru.maxigram.backend.rabbitmqcommons.model;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimilarityCalculationResult implements Serializable {

  private String firstUserId;

  private String secondUserId;

  private Double result;
}
