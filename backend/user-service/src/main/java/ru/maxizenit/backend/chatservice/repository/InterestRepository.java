package ru.maxizenit.backend.chatservice.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.maxizenit.backend.chatservice.entity.Interest;

public interface InterestRepository extends CrudRepository<Interest, Long> {

  @Query("select i from Interest i where i.id in :ids")
  Collection<Interest> findAllWhereIdIn(Collection<Long> ids);
}
