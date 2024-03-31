package ru.maxigram.backend.feedservice.repository;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.maxigram.backend.feedservice.entity.Comment;
import ru.maxigram.backend.feedservice.entity.Like;
import ru.maxigram.backend.feedservice.entity.Post;

/** Репозиторий лайков. */
public interface LikeRepository extends CrudRepository<Like, Long> {

  Optional<Like> findByPostAndAuthorId(Post post, String authorId);

  Optional<Like> findByCommentAndAuthorId(Comment comment, String authorId);

  @Query("select l from Like l where l.authorId = :authorId and l.post != null")
  Collection<Like> findAllPostLikesByAuthorId(String authorId);

  @Query("select l from Like l where l.authorId = :authorId and l.comment != null")
  Collection<Like> findAllCommentLikesByAuthorId(String authorId);
}
