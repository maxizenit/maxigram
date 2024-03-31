package ru.maxigram.backend.chatservice.service.impl;

import java.sql.Timestamp;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxigram.backend.chatservice.entity.Chat;
import ru.maxigram.backend.chatservice.entity.Message;
import ru.maxigram.backend.chatservice.repository.MessageRepository;
import ru.maxigram.backend.chatservice.service.MessageService;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;

  @Override
  public Message createMessage(Chat chat, String senderId, String text) {
    Message message = new Message();
    message.setChat(chat);
    message.setTimestamp(new Timestamp(System.currentTimeMillis()));
    message.setSenderId(senderId);
    message.setText(text);
    message.setRead(false);

    return messageRepository.save(message);
  }

  @Override
  public Collection<Message> getAllMessagesFromChat(Chat chat) {
    return messageRepository.findAllByChatOrderById(chat);
  }

  @Override
  public void saveAllMessages(Collection<Message> messages) {
    messageRepository.saveAll(messages);
  }

  @Override
  public Message getLastMessageFromChat(Chat chat) {
    return messageRepository.getTopMessageByChatOrderByIdDesc(chat);
  }
}
