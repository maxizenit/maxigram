package ru.maxizenit.backend.apiservice.controller;

import com.google.protobuf.util.Timestamps;
import java.sql.Timestamp;
import java.util.Collection;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.maxigram.backend.grpccommons.FeedServiceGrpc;
import ru.maxigram.backend.grpccommons.FeedServiceOuterClass;
import ru.maxigram.backend.grpccommons.UserServiceGrpc;
import ru.maxigram.backend.grpccommons.UserServiceOuterClass;
import ru.maxigram.backend.maxigramcommons.dto.Comment;
import ru.maxigram.backend.maxigramcommons.dto.Post;
import ru.maxigram.backend.maxigramcommons.dto.request.CreatePostRequest;

@RestController
@RequestMapping("/posts")
public class PostsController {

  @GrpcClient("feed-service")
  private FeedServiceGrpc.FeedServiceBlockingStub feedService;

  @GrpcClient("user-service")
  private UserServiceGrpc.UserServiceBlockingStub userService;

  @GetMapping
  public ResponseEntity<Collection<Post>> getFeed(@RequestParam String requesterId) {
    Collection<String> subscriptions =
        userService
            .getSubscriptionsByUserId(
                UserServiceOuterClass.GetSubscriptionsByUserIdRequest.newBuilder()
                    .setUserId(requesterId)
                    .build())
            .getSubscriptionsList()
            .stream()
            .map(UserServiceOuterClass.Subscription::getAuthorId)
            .toList();

    FeedServiceOuterClass.GetFeedResponse response =
        feedService.getFeed(
            FeedServiceOuterClass.GetFeedRequest.newBuilder()
                .setRequesterId(requesterId)
                .addAllAuthorsIds(subscriptions)
                .build());

    Collection<Post> result =
        response.getPostsList().stream()
            .map(
                responseElement ->
                    Post.builder()
                        .id(responseElement.getId())
                        .authorId(responseElement.getAuthorId())
                        .text(responseElement.getText())
                        .timestamp(
                            new Timestamp(Timestamps.toMillis(responseElement.getTimestamp())))
                        .likesCount(responseElement.getLikesCount())
                        .commentsCount(responseElement.getCommentsCount())
                        .isLikedByRequester(responseElement.getIsLikedByRequester())
                        .build())
            .toList();

    return ResponseEntity.ok(result);
  }

  @PostMapping
  public ResponseEntity<Post> createPost(@RequestBody CreatePostRequest request) {
    FeedServiceOuterClass.Post response =
        feedService.createPost(
            FeedServiceOuterClass.CreatePostRequest.newBuilder()
                .setAuthorId(request.getAuthorId())
                .setText(request.getText())
                .build());

    Post result =
        Post.builder()
            .id(response.getId())
            .authorId(response.getAuthorId())
            .text(response.getText())
            .timestamp(new Timestamp(Timestamps.toMillis(response.getTimestamp())))
            .likesCount(response.getLikesCount())
            .commentsCount(response.getCommentsCount())
            .isLikedByRequester(response.getIsLikedByRequester())
            .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Post> getPostById(@PathVariable Long id, @RequestParam String requesterId) {
    FeedServiceOuterClass.Post response =
        feedService.getPostById(
            FeedServiceOuterClass.GetPostByIdRequest.newBuilder()
                .setPostId(id)
                .setRequesterId(requesterId)
                .build());

    Post result =
        Post.builder()
            .id(response.getId())
            .authorId(response.getAuthorId())
            .text(response.getText())
            .timestamp(new Timestamp(Timestamps.toMillis(response.getTimestamp())))
            .likesCount(response.getLikesCount())
            .commentsCount(response.getCommentsCount())
            .isLikedByRequester(response.getIsLikedByRequester())
            .build();

    return ResponseEntity.ok(result);
  }

  @PostMapping("/{id}/like")
  public ResponseEntity<Post> likePost(@PathVariable Long id, @RequestParam String requesterId) {
    FeedServiceOuterClass.Post response =
        feedService.likePost(
            FeedServiceOuterClass.LikePostRequest.newBuilder()
                .setPostId(id)
                .setRequesterId(requesterId)
                .build());
    Post result =
        Post.builder()
            .id(response.getId())
            .authorId(response.getAuthorId())
            .text(response.getText())
            .timestamp(new Timestamp(Timestamps.toMillis(response.getTimestamp())))
            .likesCount(response.getLikesCount())
            .commentsCount(response.getCommentsCount())
            .isLikedByRequester(response.getIsLikedByRequester())
            .build();

    return ResponseEntity.ok(result);
  }

  @PostMapping("/{id}/unlike")
  public ResponseEntity<Post> unlikePost(@PathVariable Long id, @RequestParam String requesterId) {
    FeedServiceOuterClass.Post response =
        feedService.unlikePost(
            FeedServiceOuterClass.UnlikePostRequest.newBuilder()
                .setPostId(id)
                .setRequesterId(requesterId)
                .build());
    Post result =
        Post.builder()
            .id(response.getId())
            .authorId(response.getAuthorId())
            .text(response.getText())
            .timestamp(new Timestamp(Timestamps.toMillis(response.getTimestamp())))
            .likesCount(response.getLikesCount())
            .commentsCount(response.getCommentsCount())
            .isLikedByRequester(response.getIsLikedByRequester())
            .build();

    return ResponseEntity.ok(result);
  }

  @GetMapping("/{id}/comments")
  public ResponseEntity<Collection<Comment>> getCommentsForPost(
      @PathVariable Long id, @RequestParam String requesterId) {
    FeedServiceOuterClass.GetCommentsForPostResponse response =
        feedService.getCommentsForPost(
            FeedServiceOuterClass.GetCommentsForPostRequest.newBuilder()
                .setPostId(id)
                .setRequesterId(requesterId)
                .build());

    Collection<Comment> result =
        response.getCommentsList().stream()
            .map(
                responseElement ->
                    Comment.builder()
                        .id(responseElement.getId())
                        .postId(responseElement.getPostId())
                        .authorId(responseElement.getAuthorId())
                        .text(responseElement.getText())
                        .timestamp(
                            new Timestamp(Timestamps.toMillis(responseElement.getTimestamp())))
                        .likesCount(responseElement.getLikesCount())
                        .isLikedByRequester(responseElement.getIsLikedByRequester())
                        .build())
            .toList();

    return ResponseEntity.ok(result);
  }
}
