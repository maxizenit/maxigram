package ru.maxizenit.backend.chatservice.service;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxizenit.backend.chatservice.entity.Comment;
import ru.maxizenit.backend.chatservice.entity.Like;
import ru.maxizenit.backend.chatservice.entity.Post;
import ru.maxizenit.backend.chatservice.exception.CommentNotFoundException;
import ru.maxizenit.backend.chatservice.repository.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;

  public Comment getCommentById(long id) {
    return commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException(id));
  }

  public Comment createComment(Post post, String authorId, String text) {
    Comment comment = new Comment();
    comment.setPost(post);
    comment.setAuthorId(authorId);
    comment.setText(text);
    comment.setTimestamp(new Timestamp(System.currentTimeMillis()));

    return commentRepository.save(comment);
  }

  public Comment likeComment(long id, String requesterId) {
    Comment comment = getCommentById(id);

    if (comment.getLikes().stream().noneMatch(l -> l.getAuthorId().equals(requesterId))) {
      Like like = new Like();
      like.setComment(comment);
      like.setAuthorId(requesterId);
      comment.getLikes().add(like);

      commentRepository.save(comment);
    }

    return comment;
  }

  public Comment unlikeComment(long id, String requesterId) {
    Comment comment = getCommentById(id);
    Optional<Like> like =
        comment.getLikes().stream().filter(l -> l.getAuthorId().equals(requesterId)).findFirst();

    if (like.isPresent()) {
      comment.getLikes().remove(like.get());
      commentRepository.save(comment);
    }

    return comment;
  }

  public Collection<Comment> getCommentsForPost(Post post) {
    return commentRepository.findByPost(post);
  }

  public Collection<Long> getPostIdsCommentedByAuthor(String authorId) {
    return commentRepository.findByAuthorId(authorId).stream()
        .map(c -> c.getPost().getId())
        .distinct()
        .toList();
  }
}
