package ru.maxigram.backend.chatservice.repository;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.maxigram.backend.chatservice.entity.Chat;

/** Репозиторий чатов. */
public interface ChatRepository extends CrudRepository<Chat, Long> {

  @Query(
      "select c from Chat c where ((c.firstParticipantId = :firstParticipantId and c.secondParticipantId = :secondParticipantId) "
          + "or (c.firstParticipantId = :secondParticipantId and c.secondParticipantId = :firstParticipantId)) and c.anonymous = false")
  Optional<Chat> findByParticipantIds(String firstParticipantId, String secondParticipantId);

  @Query("select c from Chat c where (c.firstParticipantId = :userId) or (c.secondParticipantId = :userId)")
  Collection<Chat> findAllByUserId(String userId);
}
