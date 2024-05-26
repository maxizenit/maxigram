package ru.maxizenit.backend.chatservice.repository;

import java.util.Collection;
import org.springframework.data.repository.CrudRepository;
import ru.maxizenit.backend.chatservice.entity.Post;

public interface PostRepository extends CrudRepository<Post, Long> {

  Collection<Post> findByAuthorIdInOrderByTimestampDesc(Collection<String> authorsIds);
}
