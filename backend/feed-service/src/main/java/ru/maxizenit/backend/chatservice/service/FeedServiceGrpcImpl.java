package ru.maxizenit.backend.chatservice.service;

import com.google.protobuf.util.Timestamps;
import io.grpc.stub.StreamObserver;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.maxigram.backend.grpccommons.FeedServiceGrpc;
import ru.maxigram.backend.grpccommons.FeedServiceOuterClass;
import ru.maxizenit.backend.chatservice.entity.Comment;
import ru.maxizenit.backend.chatservice.entity.Post;

@GrpcService
@RequiredArgsConstructor
public class FeedServiceGrpcImpl extends FeedServiceGrpc.FeedServiceImplBase {

  private final PostService postService;
  private final CommentService commentService;
  private final FeedServiceFacade feedServiceFacade;

  @Override
  public void getFeed(
      FeedServiceOuterClass.GetFeedRequest request,
      StreamObserver<FeedServiceOuterClass.GetFeedResponse> responseObserver) {
    Collection<Post> posts = postService.getFeed(request.getAuthorsIdsList());

    FeedServiceOuterClass.GetFeedResponse response =
        FeedServiceOuterClass.GetFeedResponse.newBuilder()
            .addAllPosts(
                posts.stream()
                    .map(
                        post ->
                            FeedServiceOuterClass.Post.newBuilder()
                                .setId(post.getId())
                                .setAuthorId(post.getAuthorId())
                                .setText(post.getText())
                                .setTimestamp(Timestamps.fromMillis(post.getTimestamp().getTime()))
                                .setLikesCount(post.getLikes().size())
                                .setCommentsCount(post.getComments().size())
                                .setIsLikedByRequester(
                                    post.getLikes().stream()
                                        .anyMatch(
                                            l -> l.getAuthorId().equals(request.getRequesterId())))
                                .build())
                    .toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void createPost(
      FeedServiceOuterClass.CreatePostRequest request,
      StreamObserver<FeedServiceOuterClass.Post> responseObserver) {
    Post post = postService.createPost(request.getAuthorId(), request.getText());
    FeedServiceOuterClass.Post response =
        FeedServiceOuterClass.Post.newBuilder()
            .setId(post.getId())
            .setAuthorId(post.getAuthorId())
            .setText(post.getText())
            .setTimestamp(Timestamps.fromMillis(post.getTimestamp().getTime()))
            .setLikesCount(0)
            .setCommentsCount(0)
            .setIsLikedByRequester(false)
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getPostById(
      FeedServiceOuterClass.GetPostByIdRequest request,
      StreamObserver<FeedServiceOuterClass.Post> responseObserver) {
    Post post = postService.getPostById(request.getPostId());
    FeedServiceOuterClass.Post response =
        FeedServiceOuterClass.Post.newBuilder()
            .setId(post.getId())
            .setAuthorId(post.getAuthorId())
            .setText(post.getText())
            .setTimestamp(Timestamps.fromMillis(post.getTimestamp().getTime()))
            .setLikesCount(post.getLikes().size())
            .setCommentsCount(post.getComments().size())
            .setIsLikedByRequester(
                post.getLikes().stream()
                    .anyMatch(l -> l.getAuthorId().equals(request.getRequesterId())))
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void likePost(
      FeedServiceOuterClass.LikePostRequest request,
      StreamObserver<FeedServiceOuterClass.Post> responseObserver) {
    Post post = postService.likePost(request.getPostId(), request.getRequesterId());

    FeedServiceOuterClass.Post response =
        FeedServiceOuterClass.Post.newBuilder()
            .setId(post.getId())
            .setAuthorId(post.getAuthorId())
            .setText(post.getText())
            .setTimestamp(Timestamps.fromMillis(post.getTimestamp().getTime()))
            .setLikesCount(post.getLikes().size())
            .setCommentsCount(post.getComments().size())
            .setIsLikedByRequester(
                post.getLikes().stream()
                    .anyMatch(l -> l.getAuthorId().equals(request.getRequesterId())))
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void unlikePost(
      FeedServiceOuterClass.UnlikePostRequest request,
      StreamObserver<FeedServiceOuterClass.Post> responseObserver) {
    Post post = postService.unlikePost(request.getPostId(), request.getRequesterId());

    FeedServiceOuterClass.Post response =
        FeedServiceOuterClass.Post.newBuilder()
            .setId(post.getId())
            .setAuthorId(post.getAuthorId())
            .setText(post.getText())
            .setTimestamp(Timestamps.fromMillis(post.getTimestamp().getTime()))
            .setLikesCount(post.getLikes().size())
            .setCommentsCount(post.getComments().size())
            .setIsLikedByRequester(
                post.getLikes().stream()
                    .anyMatch(l -> l.getAuthorId().equals(request.getRequesterId())))
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getCommentsForPost(
      FeedServiceOuterClass.GetCommentsForPostRequest request,
      StreamObserver<FeedServiceOuterClass.GetCommentsForPostResponse> responseObserver) {
    Collection<Comment> comments = feedServiceFacade.getCommentsForPost(request.getPostId());

    FeedServiceOuterClass.GetCommentsForPostResponse response =
        FeedServiceOuterClass.GetCommentsForPostResponse.newBuilder()
            .addAllComments(
                comments.stream()
                    .map(
                        comment ->
                            FeedServiceOuterClass.Comment.newBuilder()
                                .setId(comment.getId())
                                .setPostId(comment.getPost().getId())
                                .setAuthorId(comment.getAuthorId())
                                .setText(comment.getText())
                                .setTimestamp(
                                    Timestamps.fromMillis(comment.getTimestamp().getTime()))
                                .setLikesCount(comment.getLikes().size())
                                .setIsLikedByRequester(
                                    comment.getLikes().stream()
                                        .anyMatch(
                                            l -> l.getAuthorId().equals(request.getRequesterId())))
                                .build())
                    .toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void addComment(
      FeedServiceOuterClass.AddCommentRequest request,
      StreamObserver<FeedServiceOuterClass.Comment> responseObserver) {
    Comment comment =
        feedServiceFacade.createComment(
            request.getPostId(), request.getAuthorId(), request.getText());

    FeedServiceOuterClass.Comment response =
        FeedServiceOuterClass.Comment.newBuilder()
            .setId(comment.getId())
            .setPostId(comment.getPost().getId())
            .setAuthorId(comment.getAuthorId())
            .setText(comment.getText())
            .setTimestamp(Timestamps.fromMillis(comment.getTimestamp().getTime()))
            .setLikesCount(0)
            .setIsLikedByRequester(false)
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void likeComment(
      FeedServiceOuterClass.LikeCommentRequest request,
      StreamObserver<FeedServiceOuterClass.Comment> responseObserver) {
    Comment comment = commentService.likeComment(request.getCommentId(), request.getRequesterId());

    FeedServiceOuterClass.Comment response =
        FeedServiceOuterClass.Comment.newBuilder()
            .setId(comment.getId())
            .setPostId(comment.getPost().getId())
            .setAuthorId(comment.getAuthorId())
            .setText(comment.getText())
            .setTimestamp(Timestamps.fromMillis(comment.getTimestamp().getTime()))
            .setLikesCount(comment.getLikes().size())
            .setIsLikedByRequester(
                comment.getLikes().stream()
                    .anyMatch(l -> l.getAuthorId().equals(request.getRequesterId())))
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void unlikeComment(
      FeedServiceOuterClass.UnlikeCommentRequest request,
      StreamObserver<FeedServiceOuterClass.Comment> responseObserver) {
    Comment comment =
        commentService.unlikeComment(request.getCommentId(), request.getRequesterId());

    FeedServiceOuterClass.Comment response =
        FeedServiceOuterClass.Comment.newBuilder()
            .setId(comment.getId())
            .setPostId(comment.getPost().getId())
            .setAuthorId(comment.getAuthorId())
            .setText(comment.getText())
            .setTimestamp(Timestamps.fromMillis(comment.getTimestamp().getTime()))
            .setLikesCount(comment.getLikes().size())
            .setIsLikedByRequester(
                comment.getLikes().stream()
                    .anyMatch(l -> l.getAuthorId().equals(request.getRequesterId())))
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
