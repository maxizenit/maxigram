package ru.maxigram.backend.feedservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxigram.backend.feedservice.entity.Like;
import ru.maxigram.backend.feedservice.entity.Post;
import ru.maxigram.backend.feedservice.exception.PostNotFoundException;
import ru.maxigram.backend.feedservice.service.LikeService;
import ru.maxigram.backend.feedservice.service.PostLikeFacade;
import ru.maxigram.backend.feedservice.service.PostService;

@Service
@RequiredArgsConstructor
public class PostLikeFacadeImpl implements PostLikeFacade {

  private final PostService postService;
  private final LikeService likeService;

  @Override
  public Like likePost(Long postId, String authorId) {
    Post post =
        postService.findPostById(postId).orElseThrow(() -> new PostNotFoundException(postId));
    return likeService.likePost(post, authorId);
  }

  @Override
  public Boolean checkPostIsLikedByUser(Long postId, String userId) {
    Post post =
        postService.findPostById(postId).orElseThrow(() -> new PostNotFoundException(postId));
    return post.getLikes().stream().anyMatch(l -> l.getAuthorId().equals(userId));
  }

  @Override
  public Integer getPostLikesCount(Long postId) {
    Post post =
        postService.findPostById(postId).orElseThrow(() -> new PostNotFoundException(postId));
    return post.getLikes().size();
  }
}
