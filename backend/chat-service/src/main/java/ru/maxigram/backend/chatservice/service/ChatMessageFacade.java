package ru.maxigram.backend.chatservice.service;

import java.util.Collection;
import java.util.Map;

import ru.maxigram.backend.chatservice.entity.Message;

/** Сервис-фасад, объединяющий {@link ChatService} и {@link MessageService}. */
public interface ChatMessageFacade {

  /**
   * Отправляет сообщение.
   *
   * @param senderId идентификатор отправителя
   * @param receiverId идентификатор получателя
   * @param text текст
   * @return отправленное сообщение
   */
  Message sendMessage(String senderId, String receiverId, String text);

  /**
   * Отправляет сообщение.
   *
   * @param senderId идентификатор отправителя
   * @param chatId идентификатор чата
   * @param text текст
   * @return отправленное сообщение
   */
  Message sendMessage(String senderId, Long chatId, String text);

  Collection<Message> getAllMessagesInChat(Long chatId, String userId);

  Map<Long, Message> getLastMessagesByChatIds(Collection<Long> chatIds);
}
