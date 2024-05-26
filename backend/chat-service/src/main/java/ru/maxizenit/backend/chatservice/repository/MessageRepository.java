package ru.maxizenit.backend.chatservice.repository;

import java.util.Collection;
import org.springframework.data.repository.CrudRepository;
import ru.maxizenit.backend.chatservice.entity.Message;

public interface MessageRepository extends CrudRepository<Message, Long> {

  Collection<Message> findByChatIdOrderById(long chatId);

  Collection<Message> findByChatIdAndRead(long chatId, boolean read);

  Message findTop1ByChatIdOrderById(long chatId);
}
