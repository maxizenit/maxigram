package ru.maxigram.backend.chatservice.service;

import java.util.ArrayDeque;
import java.util.Deque;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.maxigram.backend.chatservice.exception.UserIsAlreadyInAnonymousChatQueue;
import ru.maxigram.backend.rabbitmqcommons.RabbitMQParameters;
import ru.maxigram.backend.rabbitmqcommons.model.SimilarityCalculationRequest;
import ru.maxigram.backend.rabbitmqcommons.model.SimilarityCalculationResult;

/** Очередь заявок на поиск анонимного собеседника. */
@Service
@RequiredArgsConstructor
public class AnonymousChatQueue {

  /** Минимальное значение сходства для создания анонимного чата. */
  @Value("${minimal-similarity-for-anonymous-chat}")
  private Double minimalSimilarityForAnonymousChat;

  private final ChatService chatService;
  private final RabbitTemplate rabbitTemplate;

  /** Идентификаторы пользователей. */
  private final Deque<String> userIds = new ArrayDeque<>();

  /**
   * Добавляет пользователя в очередь на поиск анонимного собеседника.
   *
   * @param userId идентификатор пользователя
   */
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

  /**
   * Отправляет заявку на расчёт сходства между пользователями в user-service через RabbitMQ.
   *
   * @param firstUserId идентификатор первого пользователя
   * @param secondUserId идентификатор второго пользователя
   */
  private void applyCalculation(String firstUserId, String secondUserId) {
    rabbitTemplate.convertAndSend(
        RabbitMQParameters.EXCHANGE,
        RabbitMQParameters.SIMILARITY_CALCULATION_REQUEST,
        SimilarityCalculationRequest.builder()
            .firstUserId(firstUserId)
            .secondUserId(secondUserId)
            .build());
  }

  /**
   * Получает результат расчёта и при соответствии уровню сходства создаёт анонимный чат.
   *
   * @param result результат расчёта сходства из user-service
   */
  @RabbitListener(queues = RabbitMQParameters.SIMILARITY_CALCULATION_RESULT)
  public void addCalculationResult(SimilarityCalculationResult result) {
    if (result.getResult() >= minimalSimilarityForAnonymousChat
        && userIds.contains(result.getFirstUserId())
        && userIds.contains(result.getSecondUserId())) {
      createAnonymousChat(result.getFirstUserId(), result.getSecondUserId());
    }
  }

  /**
   * Создаёт анонимный чат, удаляя пользователей из очереди.
   *
   * @param firstParticipantId идентификатор первого пользователя
   * @param secondParticipantId идентификатор второго пользователя
   */
  private void createAnonymousChat(String firstParticipantId, String secondParticipantId) {
    userIds.remove(firstParticipantId);
    userIds.remove(secondParticipantId);
    chatService.createChat(firstParticipantId, secondParticipantId, true);
  }
}
