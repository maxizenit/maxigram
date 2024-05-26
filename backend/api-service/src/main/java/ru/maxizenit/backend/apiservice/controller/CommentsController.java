package ru.maxizenit.backend.apiservice.controller;

import com.google.protobuf.util.Timestamps;
import java.sql.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.maxigram.backend.grpccommons.FeedServiceGrpc;
import ru.maxigram.backend.grpccommons.FeedServiceOuterClass;
import ru.maxigram.backend.maxigramcommons.dto.Comment;
import ru.maxigram.backend.maxigramcommons.dto.request.AddCommentRequest;

@RestController
@RequestMapping("/comments")
public class CommentsController {

  @GrpcClient("feed-service")
  private FeedServiceGrpc.FeedServiceBlockingStub feedService;

  @PostMapping
  public ResponseEntity<Comment> addComment(@RequestBody AddCommentRequest request) {
    FeedServiceOuterClass.Comment response =
        feedService.addComment(
            FeedServiceOuterClass.AddCommentRequest.newBuilder()
                .setPostId(request.getPostId())
                .setAuthorId(request.getAuthorId())
                .setText(request.getText())
                .build());

    Comment result =
        Comment.builder()
            .id(response.getId())
            .postId(response.getPostId())
            .authorId(response.getAuthorId())
            .text(response.getText())
            .timestamp(new Timestamp(Timestamps.toMillis(response.getTimestamp())))
            .likesCount(response.getLikesCount())
            .isLikedByRequester(response.getIsLikedByRequester())
            .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @PostMapping("/{id}/like")
  public ResponseEntity<Comment> likeComment(
      @PathVariable Long id, @RequestParam String requesterId) {
    FeedServiceOuterClass.Comment response =
        feedService.likeComment(
            FeedServiceOuterClass.LikeCommentRequest.newBuilder()
                .setCommentId(id)
                .setRequesterId(requesterId)
                .build());

    Comment result =
        Comment.builder()
            .id(response.getId())
            .postId(response.getPostId())
            .authorId(response.getAuthorId())
            .text(response.getText())
            .timestamp(new Timestamp(Timestamps.toMillis(response.getTimestamp())))
            .likesCount(response.getLikesCount())
            .isLikedByRequester(response.getIsLikedByRequester())
            .build();

    return ResponseEntity.ok(result);
  }

  @PostMapping("/{id}/unlike")
  public ResponseEntity<Comment> unlikeComment(
      @PathVariable Long id, @RequestParam String requesterId) {
    FeedServiceOuterClass.Comment response =
        feedService.unlikeComment(
            FeedServiceOuterClass.UnlikeCommentRequest.newBuilder()
                .setCommentId(id)
                .setRequesterId(requesterId)
                .build());

    Comment result =
        Comment.builder()
            .id(response.getId())
            .postId(response.getPostId())
            .authorId(response.getAuthorId())
            .text(response.getText())
            .timestamp(new Timestamp(Timestamps.toMillis(response.getTimestamp())))
            .likesCount(response.getLikesCount())
            .isLikedByRequester(response.getIsLikedByRequester())
            .build();

    return ResponseEntity.ok(result);
  }
}
