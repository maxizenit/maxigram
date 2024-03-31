package ru.maxigram.backend.userservice.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import ru.maxigram.backend.rabbitmqcommons.RabbitMQParameters;
import ru.maxigram.backend.rabbitmqcommons.model.SimilarityCalculationRequest;
import ru.maxigram.backend.rabbitmqcommons.model.SimilarityCalculationResult;
import ru.maxigram.backend.rabbitmqcommons.model.UserActionsFromFeedServiceRequest;
import ru.maxigram.backend.rabbitmqcommons.model.UserActionsFromFeedServiceResult;
import ru.maxigram.backend.userservice.dto.SimilarityCalculation;
import ru.maxigram.backend.userservice.entity.Interest;
import ru.maxigram.backend.userservice.entity.UserProfile;
import ru.maxigram.backend.userservice.exception.UserProfileNotFoundException;

@Service
@RequiredArgsConstructor
public class SimilarityCalculator {

  private final UserProfileService userProfileService;
  private final RabbitTemplate rabbitTemplate;

  private final List<SimilarityCalculation> similarityCalculations;

  @RabbitListener(queues = RabbitMQParameters.SIMILARITY_CALCULATION_REQUEST)
  public void startCalculation(SimilarityCalculationRequest request) {
    String firstUserId = request.getFirstUserId();
    String secondUserId = request.getSecondUserId();

    UserProfile firstUserProfile =
        userProfileService
            .findUserProfileById(firstUserId)
            .orElseThrow(() -> new UserProfileNotFoundException(firstUserId));
    UserProfile secondUserProfile =
        userProfileService
            .findUserProfileById(secondUserId)
            .orElseThrow(() -> new UserProfileNotFoundException(secondUserId));

    SimilarityCalculation calculation = new SimilarityCalculation();
    calculation.setFirstUserId(firstUserId);
    calculation.setSecondUserId(secondUserId);
    calculation.setAgeSimilarity(
        calculateAgeSimilarity(firstUserProfile.getBirthdate(), secondUserProfile.getBirthdate()));
    calculation.setInterestsSimilarity(
        calculateInterestsSimilarity(
            firstUserProfile.getInterests(), secondUserProfile.getInterests()));

    similarityCalculations.add(calculation);

    rabbitTemplate.convertAndSend(
        RabbitMQParameters.EXCHANGE,
        RabbitMQParameters.USER_ACTIONS_FROM_FEED_SERVICE_REQUEST,
        UserActionsFromFeedServiceRequest.builder()
            .userId(firstUserId)
            .firstUserId(firstUserId)
            .secondUserId(secondUserId)
            .build());
    rabbitTemplate.convertAndSend(
        RabbitMQParameters.EXCHANGE,
        RabbitMQParameters.USER_ACTIONS_FROM_FEED_SERVICE_REQUEST,
        UserActionsFromFeedServiceRequest.builder()
            .userId(secondUserId)
            .firstUserId(firstUserId)
            .secondUserId(secondUserId)
            .build());
  }

  @RabbitListener(queues = RabbitMQParameters.USER_ACTIONS_FROM_FEED_SERVICE_RESULT)
  public void addFeedActionsInSimilarityCalculation(
      UserActionsFromFeedServiceResult userActionsFromFeedServiceResult) {
    SimilarityCalculation calculation =
        similarityCalculations.stream()
            .filter(
                c ->
                    c.getFirstUserId().equals(userActionsFromFeedServiceResult.getFirstUserId())
                        && c.getSecondUserId()
                            .equals(userActionsFromFeedServiceResult.getSecondUserId()))
            .findAny()
            .orElseThrow();

    if (userActionsFromFeedServiceResult.getUserId().equals(calculation.getFirstUserId())) {
      calculation.setFirstUserCommentedPostIds(
          userActionsFromFeedServiceResult.getCommentedPostIds());
      calculation.setFirstUserLikedPostIds(userActionsFromFeedServiceResult.getLikedPostIds());
      calculation.setFirstUserLikedCommentsIds(
          userActionsFromFeedServiceResult.getLikedCommentsIds());
      calculation.setFirstUserFeedInformationCompleted(true);
    } else {
      calculation.setSecondUserCommentedPostIds(
          userActionsFromFeedServiceResult.getCommentedPostIds());
      calculation.setSecondUserLikedPostIds(userActionsFromFeedServiceResult.getLikedPostIds());
      calculation.setSecondUserLikedCommentsIds(
          userActionsFromFeedServiceResult.getLikedCommentsIds());
      calculation.setSecondUserFeedInformationCompleted(true);
    }

    if (calculation.getFirstUserFeedInformationCompleted()
        && calculation.getSecondUserFeedInformationCompleted()) {
      endCalculation(calculation);
    }
  }

  public void endCalculation(SimilarityCalculation calculation) {
    calculation.setFeedActionsSimilarity(calculateFeedActionsSimilarity(calculation));

    double result =
        0.5 * calculation.getAgeSimilarity()
            + 0.25 * calculation.getInterestsSimilarity()
            + 0.25 * calculation.getFeedActionsSimilarity();
    rabbitTemplate.convertAndSend(
        RabbitMQParameters.EXCHANGE,
        RabbitMQParameters.SIMILARITY_CALCULATION_RESULT,
        SimilarityCalculationResult.builder()
            .firstUserId(calculation.getFirstUserId())
            .secondUserId(calculation.getSecondUserId())
            .result(result)
            .build());

    similarityCalculations.remove(calculation);
  }

  private double calculateAgeSimilarity(Date firstBirthdate, Date secondBirthdate) {
    int firstAge = calculateAge(firstBirthdate);
    int secondAge = calculateAge(secondBirthdate);

    return Math.max(1.0 - (Math.abs(firstAge - secondAge) * 0.05), 0);
  }

  private int calculateAge(Date birthdate) {
    return new Date().getYear() - birthdate.getYear();
  }

  private double calculateInterestsSimilarity(
      Collection<Interest> firstUserInterests, Collection<Interest> secondUserInterests) {
    int firstUserInterestsCount = firstUserInterests.size();
    int secondUserInterestsCount = secondUserInterests.size();
    long commonInterests =
        firstUserInterests.stream().filter(secondUserInterests::contains).count();

    double result =
        (((double) commonInterests / firstUserInterestsCount)
                + ((double) secondUserInterestsCount / commonInterests))
            / 2;

    return Double.isNaN(result) ? 0 : result;
  }

  private double calculateFeedActionsSimilarity(SimilarityCalculation calculation) {
    double result =
        0.5
                * calculateCommentsSimilarity(
                    calculation.getFirstUserCommentedPostIds(),
                    calculation.getSecondUserCommentedPostIds())
            + 0.5
                * calculateLikesSimilarity(
                    calculation.getFirstUserLikedPostIds(),
                    calculation.getFirstUserLikedCommentsIds(),
                    calculation.getSecondUserLikedPostIds(),
                    calculation.getSecondUserLikedCommentsIds());

    return Double.isNaN(result) ? 0 : result;
  }

  private double calculateCommentsSimilarity(
      Collection<Long> firstUserCommentedPostIds, Collection<Long> secondUserCommentedPostIds) {
    int firstUserCommentedPostCount = firstUserCommentedPostIds.size();
    int secondUserCommentedPostCount = secondUserCommentedPostIds.size();
    long commonCommentedPostCount =
        firstUserCommentedPostIds.stream().filter(secondUserCommentedPostIds::contains).count();

    double result =
        (((double) commonCommentedPostCount / firstUserCommentedPostCount)
                + ((double) commonCommentedPostCount / secondUserCommentedPostCount))
            / 2;

    return Double.isNaN(result) ? 0 : result;
  }

  private double calculateLikesSimilarity(
      Collection<Long> firstUserLikedPostIds,
      Collection<Long> firstUserLikedCommentsIds,
      Collection<Long> secondUserLikedPostIds,
      Collection<Long> secondUserLikedCommentsIds) {
    int firstUserLikedPostCount = firstUserLikedPostIds.size();
    int secondUserLikedPostCount = secondUserLikedPostIds.size();
    long commonLikedPostCount =
        firstUserLikedPostIds.stream().filter(secondUserLikedPostIds::contains).count();
    double likedPostSimilarity =
        (((double) commonLikedPostCount / firstUserLikedPostCount)
                + ((double) commonLikedPostCount / secondUserLikedPostCount))
            / 2;

    int firstUserLikedCommentsCount = firstUserLikedCommentsIds.size();
    int secondUserLikedCommentsCount = secondUserLikedCommentsIds.size();
    long commonLikedCommentsCount =
        firstUserLikedCommentsIds.stream().filter(secondUserLikedCommentsIds::contains).count();
    double likedCommentsSimilarity =
        (((double) commonLikedCommentsCount / firstUserLikedCommentsCount)
                + ((double) commonLikedCommentsCount / secondUserLikedCommentsCount))
            / 2;

    double result = (likedPostSimilarity + likedCommentsSimilarity) / 2;
    return Double.isNaN(result) ? 0 : result;
  }
}
