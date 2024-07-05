package ru.maxizenit.backend.chatservice.repository;

import java.util.Collection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.maxizenit.backend.chatservice.entity.Like;

public interface LikeRepository extends CrudRepository<Like, Long> {

  @Query("select l from Like l where l.authorId = :authorId and l.post != null")
  Collection<Like> findAllPostLikesByAuthorId(String authorId);

  @Query("select l from Like l where l.authorId = :authorId and l.comment != null")
  Collection<Like> findAllCommentLikesByAuthorId(String authorId);
}
