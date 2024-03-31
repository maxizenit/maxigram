package ru.maxigram.backend.chatservice.service;

import java.util.Collection;
import ru.maxigram.backend.chatservice.entity.Chat;
import ru.maxigram.backend.chatservice.entity.Message;

/** Сервис для работы с сообщениями. */
public interface MessageService {

  /**
   * Создаёт сообщение.
   *
   * @param chat чат
   * @param senderId идентификатор отправителя
   * @param text текст
   * @return созданное сообщение
   */
  Message createMessage(Chat chat, String senderId, String text);

  Collection<Message> getAllMessagesFromChat(Chat chat);

  void saveAllMessages(Collection<Message> messages);

  Message getLastMessageFromChat(Chat chat);
}
