package ru.maxizenit.backend.chatservice.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.maxizenit.backend.chatservice.entity.Chat;

import java.util.Collection;

public interface ChatRepository extends CrudRepository<Chat, Long> {

    @Query("select c from Chat c where ((c.firstParticipantId = :firstParticipantId and c.secondParticipantId = :secondParticipantId) " +
            "or (c.firstParticipantId = :secondParticipantId and c.secondParticipantId = :firstParticipantId)) and c.anonymous = false")
    Chat findNonAnonymousChatByParticipantIds(String firstParticipantId, String secondParticipantId);

    @Query("select c from Chat c where c.firstParticipantId = :participantId or c.secondParticipantId = :participantId")
    Collection<Chat> findByParticipantId(String participantId);
}
