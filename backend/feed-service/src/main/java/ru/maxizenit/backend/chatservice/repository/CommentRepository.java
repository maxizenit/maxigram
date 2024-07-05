package ru.maxizenit.backend.chatservice.repository;

import java.util.Collection;
import org.springframework.data.repository.CrudRepository;
import ru.maxizenit.backend.chatservice.entity.Comment;
import ru.maxizenit.backend.chatservice.entity.Post;

public interface CommentRepository extends CrudRepository<Comment, Long> {

  Collection<Comment> findByPost(Post post);

  Collection<Comment> findByAuthorId(String authorId);
}
