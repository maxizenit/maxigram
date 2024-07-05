package ru.maxigram.backend.maxigramcommons.rabbit;

public class RabbitParams {

  public static final String EXCHANGE = "exchange";
  public static final String SIMILARITY_CALCULATION_REQUEST = "similarityCalculationRequestQueue";
  public static final String SIMILARITY_CALCULATION_RESULT = "similarityCalculationResultQueue";
  public static final String USER_ACTIONS_FROM_FEED_SERVICE_REQUEST =
      "userActionsFromFeedServiceRequestQueue";
  public static final String USER_ACTIONS_FROM_FEED_SERVICE_RESULT =
      "userActionsFromFeedServiceResultQueue";
}
