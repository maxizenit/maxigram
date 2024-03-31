package ru.maxigram.backend.feedservice.service.impl;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxigram.backend.feedservice.entity.Comment;
import ru.maxigram.backend.feedservice.entity.Like;
import ru.maxigram.backend.feedservice.entity.Post;
import ru.maxigram.backend.feedservice.repository.LikeRepository;
import ru.maxigram.backend.feedservice.service.LikeService;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

  private final LikeRepository likeRepository;

  @Override
  public Like likePost(Post post, String authorId) {
    Optional<Like> existingLike = likeRepository.findByPostAndAuthorId(post, authorId);
    if (existingLike.isPresent()) {
      return existingLike.get();
    }

    Like like = new Like();
    like.setAuthorId(authorId);
    like.setPost(post);

    return likeRepository.save(like);
  }

  @Override
  public Like likeComment(Comment comment, String authorId) {
    Optional<Like> existingLike = likeRepository.findByCommentAndAuthorId(comment, authorId);
    if (existingLike.isPresent()) {
      return existingLike.get();
    }

    Like like = new Like();
    like.setAuthorId(authorId);
    like.setComment(comment);

    return likeRepository.save(like);
  }

  @Override
  public Collection<Long> getPostIdsLikedByUser(String userId) {
    return likeRepository.findAllPostLikesByAuthorId(userId).stream()
        .map(l -> l.getPost().getId())
        .collect(Collectors.toSet());
  }

  @Override
  public Collection<Long> getCommentIdsLikedByUser(String userId) {
    return likeRepository.findAllCommentLikesByAuthorId(userId).stream()
        .map(l -> l.getComment().getId())
        .collect(Collectors.toSet());
  }
}
