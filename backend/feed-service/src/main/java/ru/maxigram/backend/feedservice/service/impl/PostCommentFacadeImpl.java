package ru.maxigram.backend.feedservice.service.impl;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxigram.backend.feedservice.entity.Comment;
import ru.maxigram.backend.feedservice.entity.Post;
import ru.maxigram.backend.feedservice.exception.PostNotFoundException;
import ru.maxigram.backend.feedservice.service.CommentService;
import ru.maxigram.backend.feedservice.service.PostCommentFacade;
import ru.maxigram.backend.feedservice.service.PostService;

@Service
@RequiredArgsConstructor
public class PostCommentFacadeImpl implements PostCommentFacade {

  private final PostService postService;
  private final CommentService commentService;

  @Override
  public Comment createComment(String authorId, String text, Long postId) {
    Post post =
        postService.findPostById(postId).orElseThrow(() -> new PostNotFoundException(postId));
    return commentService.createComment(authorId, text, post);
  }

  @Override
  public Collection<Comment> findCommentsByPostId(Long postId) {
    Post post =
        postService.findPostById(postId).orElseThrow(() -> new PostNotFoundException(postId));
    return commentService.findCommentsByPost(post);
  }
}
