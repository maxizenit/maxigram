package ru.maxigram.backend.feedservice.service.impl;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxigram.backend.feedservice.entity.Comment;
import ru.maxigram.backend.feedservice.entity.Post;
import ru.maxigram.backend.feedservice.repository.CommentRepository;
import ru.maxigram.backend.feedservice.service.CommentService;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;

  @Override
  public Comment createComment(String authorId, String text, Post post) {
    Comment comment = new Comment();
    comment.setAuthorId(authorId);
    comment.setText(text);
    comment.setTimestamp(new Timestamp(System.currentTimeMillis()));
    comment.setPost(post);

    return commentRepository.save(comment);
  }

  @Override
  public Optional<Comment> findCommentById(Long id) {
    return commentRepository.findById(id);
  }

  @Override
  public Collection<Comment> findCommentsByPost(Post post) {
    return commentRepository.findAllByPostIdOrderByTimestamp(post.getId());
  }

  @Override
  public Collection<Long> getPostIdsCommentedByAuthor(String authorId) {
    Collection<Comment> comments = commentRepository.findAllByAuthorId(authorId);
    return comments.stream().map(c -> c.getPost().getId()).toList();
  }
}
