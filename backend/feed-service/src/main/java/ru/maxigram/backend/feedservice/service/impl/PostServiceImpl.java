package ru.maxigram.backend.feedservice.service.impl;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxigram.backend.feedservice.entity.Post;
import ru.maxigram.backend.feedservice.repository.PostRepository;
import ru.maxigram.backend.feedservice.service.PostService;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

  private final PostRepository postRepository;

  @Override
  public Post createPost(String authorId, String text) {
    Post post = new Post();
    post.setAuthorId(authorId);
    post.setText(text);
    post.setTimestamp(new Timestamp(System.currentTimeMillis()));

    return postRepository.save(post);
  }

  @Override
  public Optional<Post> findPostById(Long id) {
    return postRepository.findById(id);
  }

  @Override
  public Collection<Post> findPostsByAuthorIds(Collection<String> authorIds) {
    return postRepository.findAllByAuthorIds(authorIds);
  }
}
