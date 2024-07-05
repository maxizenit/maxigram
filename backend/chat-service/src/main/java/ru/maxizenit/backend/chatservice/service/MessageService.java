package ru.maxizenit.backend.chatservice.service;

import java.sql.Timestamp;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.maxizenit.backend.chatservice.entity.Chat;
import ru.maxizenit.backend.chatservice.entity.Message;
import ru.maxizenit.backend.chatservice.repository.MessageRepository;

@Service
@RequiredArgsConstructor
public class MessageService {

  private final MessageRepository messageRepository;

  public Message createMessage(Chat chat, String senderId, String text) {
    Message message = new Message();
    message.setChat(chat);
    message.setSenderId(senderId);
    message.setText(text);
    message.setTimestamp(new Timestamp(System.currentTimeMillis()));
    message.setRead(false);
    return messageRepository.save(message);
  }

  public Message getLastMessageOfChat(Chat chat) {
    return messageRepository.findTop1ByChatIdOrderById(chat.getId());
  }

  public Collection<Message> getMessagesOfChat(Chat chat, String requesterId) {
    Collection<Message> unreadMessages = messageRepository.findByChatIdAndRead(chat.getId(), false);
    if (!CollectionUtils.isEmpty(unreadMessages)
        && unreadMessages.stream().anyMatch(m -> !m.getSenderId().equals(requesterId))) {
      unreadMessages.forEach(m -> m.setRead(true));
      messageRepository.saveAll(unreadMessages);
    }

    return messageRepository.findByChatIdOrderById(chat.getId());
  }
}
