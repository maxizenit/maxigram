package ru.maxigram.backend.feedservice.repository;

import java.util.Collection;
import org.springframework.data.repository.CrudRepository;
import ru.maxigram.backend.feedservice.entity.Comment;

/** Репозиторий комментариев. */
public interface CommentRepository extends CrudRepository<Comment, Long> {

  Collection<Comment> findAllByPostIdOrderByTimestamp(Long postId);

  Collection<Comment> findAllByAuthorId(String authorId);
}
