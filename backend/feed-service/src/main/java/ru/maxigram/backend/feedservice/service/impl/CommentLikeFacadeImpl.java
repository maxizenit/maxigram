package ru.maxigram.backend.feedservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import ru.maxigram.backend.feedservice.entity.Comment;
import ru.maxigram.backend.feedservice.entity.Like;
import ru.maxigram.backend.feedservice.exception.CommentNotFoundException;
import ru.maxigram.backend.feedservice.service.CommentLikeFacade;
import ru.maxigram.backend.feedservice.service.CommentService;
import ru.maxigram.backend.feedservice.service.LikeService;
import ru.maxigram.backend.rabbitmqcommons.RabbitMQParameters;
import ru.maxigram.backend.rabbitmqcommons.model.UserActionsFromFeedServiceRequest;
import ru.maxigram.backend.rabbitmqcommons.model.UserActionsFromFeedServiceResult;

@Service
@RequiredArgsConstructor
public class CommentLikeFacadeImpl implements CommentLikeFacade {

  private final CommentService commentService;
  private final LikeService likeService;
  private final RabbitTemplate rabbitTemplate;

  @Override
  public Like likeComment(Long commentId, String authorId) {
    Comment comment =
        commentService
            .findCommentById(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));
    return likeService.likeComment(comment, authorId);
  }

  @Override
  public Boolean checkCommentIsLikedByUser(Long commentId, String userId) {
    Comment comment =
        commentService
            .findCommentById(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));
    return comment.getLikes().stream().anyMatch(l -> l.getAuthorId().equals(userId));
  }

  @Override
  public Integer getCommentLikesCount(Long commentId) {
    Comment comment =
        commentService
            .findCommentById(commentId)
            .orElseThrow(() -> new CommentNotFoundException(commentId));
    return comment.getLikes().size();
  }

  @RabbitListener(queues = RabbitMQParameters.USER_ACTIONS_FROM_FEED_SERVICE_REQUEST)
  @Override
  public void sendUserActionsForCalculate(UserActionsFromFeedServiceRequest request) {
    String userId = request.getUserId();

    UserActionsFromFeedServiceResult result =
        UserActionsFromFeedServiceResult.builder()
            .userId(userId)
            .firstUserId(request.getFirstUserId())
            .secondUserId(request.getSecondUserId())
            .commentedPostIds(commentService.getPostIdsCommentedByAuthor(userId))
            .likedPostIds(likeService.getPostIdsLikedByUser(userId))
            .likedCommentsIds(likeService.getCommentIdsLikedByUser(userId))
            .build();

    rabbitTemplate.convertAndSend(
        RabbitMQParameters.EXCHANGE,
        RabbitMQParameters.USER_ACTIONS_FROM_FEED_SERVICE_RESULT,
        result);
  }
}
