package ru.maxigram.backend.feedservice.repository;

import java.util.Collection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.maxigram.backend.feedservice.entity.Post;

/** Репозиторий постов. */
public interface PostRepository extends CrudRepository<Post, Long> {

    @Query("select p from Post p where p.authorId in :authorIds order by p.timestamp desc")
    Collection<Post> findAllByAuthorIds(Collection<String> authorIds);
}
