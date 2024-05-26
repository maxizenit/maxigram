package ru.maxizenit.backend.chatservice.service;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxizenit.backend.chatservice.entity.Like;
import ru.maxizenit.backend.chatservice.entity.Post;
import ru.maxizenit.backend.chatservice.exception.PostNotFoundException;
import ru.maxizenit.backend.chatservice.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;

  public Post getPostById(long id) {
    return postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
  }

  public Post createPost(String authorId, String text) {
    Post post = new Post();
    post.setAuthorId(authorId);
    post.setText(text);
    post.setTimestamp(new Timestamp(System.currentTimeMillis()));

    return postRepository.save(post);
  }

  public Collection<Post> getFeed(Collection<String> authorsIds) {
    return postRepository.findByAuthorIdInOrderByTimestampDesc(authorsIds);
  }

  public Post likePost(long id, String requesterId) {
    Post post = getPostById(id);

    if (post.getLikes().stream().noneMatch(l -> l.getAuthorId().equals(requesterId))) {
      Like like = new Like();
      like.setPost(post);
      like.setAuthorId(requesterId);
      post.getLikes().add(like);

      postRepository.save(post);
    }

    return post;
  }

  public Post unlikePost(long id, String requesterId) {
    Post post = getPostById(id);
    Optional<Like> like =
        post.getLikes().stream().filter(l -> l.getAuthorId().equals(requesterId)).findFirst();

    if (like.isPresent()) {
      post.getLikes().remove(like.get());
      postRepository.save(post);
    }

    return post;
  }
}
