package ru.maxizenit.backend.chatservice.service;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import ru.maxigram.backend.maxigramcommons.rabbit.RabbitParams;
import ru.maxigram.backend.maxigramcommons.rabbit.dto.UserActionsFromFeedServiceRequest;
import ru.maxigram.backend.maxigramcommons.rabbit.dto.UserActionsFromFeedServiceResult;
import ru.maxizenit.backend.chatservice.entity.Comment;
import ru.maxizenit.backend.chatservice.entity.Post;

@Service
@RequiredArgsConstructor
public class FeedServiceFacade {

  private final PostService postService;
  private final CommentService commentService;
  private final LikeService likeService;
  private final RabbitTemplate rabbitTemplate;

  public Comment createComment(long postId, String authorId, String text) {
    Post post = postService.getPostById(postId);
    return commentService.createComment(post, authorId, text);
  }

  public Collection<Comment> getCommentsForPost(long postId) {
    return commentService.getCommentsForPost(postService.getPostById(postId));
  }

  @RabbitListener(queues = RabbitParams.USER_ACTIONS_FROM_FEED_SERVICE_REQUEST)
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
        RabbitParams.EXCHANGE, RabbitParams.USER_ACTIONS_FROM_FEED_SERVICE_RESULT, result);
  }
}
