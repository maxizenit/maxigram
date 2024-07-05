package ru.maxizenit.backend.chatservice.service;

import java.util.ArrayDeque;
import java.util.Deque;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.maxigram.backend.maxigramcommons.rabbit.dto.SimilarityCalculationRequest;
import ru.maxigram.backend.maxigramcommons.rabbit.dto.SimilarityCalculationResult;
import ru.maxigram.backend.maxigramcommons.rabbit.RabbitParams;
import ru.maxizenit.backend.chatservice.exception.UserIsAlreadyInAnonymousChatQueue;

@Component
@RequiredArgsConstructor
public class AnonymousChatQueue {

  @Value("${minimal-similarity-for-anonymous-chat}")
  private Double minimalSimilarityForAnonymousChat;

  private final ChatService chatService;
  private final RabbitTemplate rabbitTemplate;
  private final Deque<String> userIds = new ArrayDeque<>();

  public void addUserInQueue(String userId) {
    if (userIds.contains(userId)) {
      throw new UserIsAlreadyInAnonymousChatQueue(userId);
    }

    userIds.addLast(userId);
    for (String otherUserId : userIds) {
      if (!userId.equals(otherUserId)) {
        applyCalculation(userId, otherUserId);
      }
    }
  }

  private void applyCalculation(String firstUserId, String secondUserId) {
    rabbitTemplate.convertAndSend(
        RabbitParams.EXCHANGE,
        RabbitParams.SIMILARITY_CALCULATION_REQUEST,
        SimilarityCalculationRequest.builder()
            .firstUserId(firstUserId)
            .secondUserId(secondUserId)
            .build());
  }

  @RabbitListener(queues = RabbitParams.SIMILARITY_CALCULATION_RESULT)
  public void addCalculationResult(SimilarityCalculationResult result) {
    if (result.getResult() >= minimalSimilarityForAnonymousChat
        && userIds.contains(result.getFirstUserId())
        && userIds.contains(result.getSecondUserId())) {
      createAnonymousChat(result.getFirstUserId(), result.getSecondUserId());
    }
  }

  private void createAnonymousChat(String firstParticipantId, String secondParticipantId) {
    userIds.remove(firstParticipantId);
    userIds.remove(secondParticipantId);
    chatService.createChat(firstParticipantId, secondParticipantId, true);
  }
}
