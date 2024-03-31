package ru.maxigram.backend.rabbitmqcommons.model;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserActionsFromFeedServiceRequest implements Serializable {

  private String userId;

  private String firstUserId;

  private String secondUserId;
}
