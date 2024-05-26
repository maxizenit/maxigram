package ru.maxizenit.backend.chatservice.service;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxizenit.backend.chatservice.repository.LikeRepository;

@Service
@RequiredArgsConstructor
public class LikeService {

  private final LikeRepository likeRepository;

  public Collection<Long> getPostIdsLikedByUser(String userId) {
    return likeRepository.findAllPostLikesByAuthorId(userId).stream()
        .map(l -> l.getPost().getId())
        .distinct()
        .toList();
  }

  public Collection<Long> getCommentIdsLikedByUser(String userId) {
    return likeRepository.findAllCommentLikesByAuthorId(userId).stream()
        .map(l -> l.getComment().getId())
        .distinct()
        .toList();
  }
}
