package ru.maxigram.backend.chatservice.repository;

import java.util.Collection;
import org.springframework.data.repository.CrudRepository;
import ru.maxigram.backend.chatservice.entity.Chat;
import ru.maxigram.backend.chatservice.entity.Message;

/** Репозиторий сообщений. */
public interface MessageRepository extends CrudRepository<Message, Long> {

  Collection<Message> findAllByChatOrderById(Chat chat);

  Message getTopMessageByChatOrderByIdDesc(Chat chat);
}
