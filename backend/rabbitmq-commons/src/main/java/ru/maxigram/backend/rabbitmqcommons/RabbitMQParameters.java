package ru.maxigram.backend.rabbitmqcommons;

public class RabbitMQParameters {

  public static final String EXCHANGE = "exchange";

  /** Очередь запросов на расчёт сходства между пользователями. */
  public static final String SIMILARITY_CALCULATION_REQUEST = "similarityCalculationRequestQueue";

  /** Очередь результатов запросов на расчёт сходства между пользователями. */
  public static final String SIMILARITY_CALCULATION_RESULT = "similarityCalculationResultQueue";

  /** Очередь запросов на получение действий пользователя из feed-service. */
  public static final String USER_ACTIONS_FROM_FEED_SERVICE_REQUEST =
      "userActionsFromFeedServiceRequestQueue";

  /** Очередь результатов запросов на получение действий пользователя из feed-service. */
  public static final String USER_ACTIONS_FROM_FEED_SERVICE_RESULT =
      "userActionsFromFeedServiceResultQueue";

  /** Очередь обновления чатов. */
  public static final String UPDATE_CHAT = "updateChatQueue";
}
