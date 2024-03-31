package ru.maxigram.backend.feedservice.grpc;

import com.google.protobuf.util.Timestamps;
import io.grpc.stub.StreamObserver;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.maxigram.backend.feedservice.entity.Comment;
import ru.maxigram.backend.feedservice.entity.Like;
import ru.maxigram.backend.feedservice.entity.Post;
import ru.maxigram.backend.feedservice.service.*;
import ru.maxigram.backend.grpccommons.FeedServiceGrpc;
import ru.maxigram.backend.grpccommons.FeedServiceOuterClass;

@GrpcService
@RequiredArgsConstructor
public class FeedServiceGrpcImpl extends FeedServiceGrpc.FeedServiceImplBase {

  private final PostService postService;
  private final PostCommentFacade postCommentFacade;
  private final PostLikeFacade postLikeFacade;
  private final CommentLikeFacade commentLikeFacade;

  @Override
  public void createPost(
      FeedServiceOuterClass.CreatePostRequest request,
      StreamObserver<FeedServiceOuterClass.Post> responseObserver) {
    Post post = postService.createPost(request.getAuthorId(), request.getText());
    FeedServiceOuterClass.Post result =
        FeedServiceOuterClass.Post.newBuilder()
            .setId(post.getId())
            .setAuthorId(post.getAuthorId())
            .setText(post.getText())
            .setTimestamp(Timestamps.fromMillis(post.getTimestamp().getTime()))
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void createComment(
      FeedServiceOuterClass.CreateCommentRequest request,
      StreamObserver<FeedServiceOuterClass.Comment> responseObserver) {
    Comment comment =
        postCommentFacade.createComment(
            request.getAuthorId(), request.getText(), request.getPostId());
    FeedServiceOuterClass.Comment result =
        FeedServiceOuterClass.Comment.newBuilder()
            .setId(comment.getId())
            .setAuthorId(comment.getAuthorId())
            .setText(comment.getText())
            .setTimestamp(Timestamps.fromMillis(comment.getTimestamp().getTime()))
            .setPostId(comment.getPost().getId())
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void likePost(
      FeedServiceOuterClass.LikePostRequest request,
      StreamObserver<FeedServiceOuterClass.Like> responseObserver) {
    Like like = postLikeFacade.likePost(request.getPostId(), request.getAuthorId());
    FeedServiceOuterClass.Like result =
        FeedServiceOuterClass.Like.newBuilder()
            .setId(like.getId())
            .setAuthorId(like.getAuthorId())
            .setPostId(like.getPost().getId())
            .setCommentId(-1)
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void likeComment(
      FeedServiceOuterClass.LikeCommentRequest request,
      StreamObserver<FeedServiceOuterClass.Like> responseObserver) {
    Like like = commentLikeFacade.likeComment(request.getCommentId(), request.getAuthorId());
    FeedServiceOuterClass.Like result =
        FeedServiceOuterClass.Like.newBuilder()
            .setId(like.getId())
            .setAuthorId(like.getAuthorId())
            .setPostId(-1)
            .setCommentId(like.getComment().getId())
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void findPostsByAuthorId(
      FeedServiceOuterClass.FindPostsByAuthorIdsRequest request,
      StreamObserver<FeedServiceOuterClass.FindPostsByAuthorIdsResponse> responseObserver) {
    Collection<Post> posts = postService.findPostsByAuthorIds(request.getAuthorIdsList());
    FeedServiceOuterClass.FindPostsByAuthorIdsResponse result =
        FeedServiceOuterClass.FindPostsByAuthorIdsResponse.newBuilder()
            .addAllPosts(
                posts.stream()
                    .map(
                        p ->
                            FeedServiceOuterClass.Post.newBuilder()
                                .setId(p.getId())
                                .setAuthorId(p.getAuthorId())
                                .setText(p.getText())
                                .setTimestamp(Timestamps.fromMillis(p.getTimestamp().getTime()))
                                .build())
                    .toList())
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void findCommentsByPostId(
      FeedServiceOuterClass.FindCommentsByPostIdRequest request,
      StreamObserver<FeedServiceOuterClass.FindCommentsByPostIdResponse> responseObserver) {
    Collection<Comment> comments = postCommentFacade.findCommentsByPostId(request.getPostId());
    FeedServiceOuterClass.FindCommentsByPostIdResponse result =
        FeedServiceOuterClass.FindCommentsByPostIdResponse.newBuilder()
            .addAllComments(
                comments.stream()
                    .map(
                        c ->
                            FeedServiceOuterClass.Comment.newBuilder()
                                .setId(c.getId())
                                .setAuthorId(c.getAuthorId())
                                .setText(c.getText())
                                .setTimestamp(Timestamps.fromMillis(c.getTimestamp().getTime()))
                                .setPostId(c.getPost().getId())
                                .build())
                    .toList())
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void checkPostIsLikedByUser(
      FeedServiceOuterClass.CheckPostIsLikedByUserRequest request,
      StreamObserver<FeedServiceOuterClass.CheckPostIsLikedByUserResponse> responseObserver) {
    FeedServiceOuterClass.CheckPostIsLikedByUserResponse result =
        FeedServiceOuterClass.CheckPostIsLikedByUserResponse.newBuilder()
            .setIsLiked(
                postLikeFacade.checkPostIsLikedByUser(request.getPostId(), request.getUserId()))
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void checkCommentIsLikedByUser(
      FeedServiceOuterClass.CheckCommentIsLikedByUserRequest request,
      StreamObserver<FeedServiceOuterClass.CheckCommentIsLikedByUserResponse> responseObserver) {
    FeedServiceOuterClass.CheckCommentIsLikedByUserResponse result =
        FeedServiceOuterClass.CheckCommentIsLikedByUserResponse.newBuilder()
            .setIsLiked(
                commentLikeFacade.checkCommentIsLikedByUser(
                    request.getCommentId(), request.getUserId()))
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void getPostLikesCount(
      FeedServiceOuterClass.GetPostLikesCountRequest request,
      StreamObserver<FeedServiceOuterClass.GetPostLikesCountResponse> responseObserver) {
    FeedServiceOuterClass.GetPostLikesCountResponse result =
        FeedServiceOuterClass.GetPostLikesCountResponse.newBuilder()
            .setLikesCount(postLikeFacade.getPostLikesCount(request.getPostId()))
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void getCommentLikesCount(
      FeedServiceOuterClass.GetCommentLikesCountRequest request,
      StreamObserver<FeedServiceOuterClass.GetCommentLikesCountResponse> responseObserver) {
    FeedServiceOuterClass.GetCommentLikesCountResponse result =
        FeedServiceOuterClass.GetCommentLikesCountResponse.newBuilder()
            .setLikesCount(commentLikeFacade.getCommentLikesCount(request.getCommentId()))
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }
}
