package ru.maxigram.backend.chatservice.service;

import java.util.Collection;
import java.util.Optional;
import ru.maxigram.backend.chatservice.entity.Chat;

/** Сервис для работы с чатами. */
public interface ChatService {

  /**
   * Создаёт чат.
   *
   * @param firstParticipantId идентификатор первого собеседника
   * @param secondParticipantId идентификатор второго собеседника
   * @param anonymous флаг анонимности
   * @return созданный чат
   */
  Chat createChat(String firstParticipantId, String secondParticipantId, Boolean anonymous);

  /**
   * Ищет чат по идентификатору.
   *
   * @param id идентификатор чата
   * @return {@link Optional} c чатом с указанным идентификатором
   */
  Optional<Chat> findChatById(Long id);

  /**
   * Ищет чат по идентификаторам собеседников.
   *
   * @param firstParticipantId идентификатор первого собеседника
   * @param secondParticipantId идентификатор второго собеседника
   * @return {@link Optional} с чатом собеседников с указанными идентификаторами
   */
  Optional<Chat> findChatByParticipantIds(String firstParticipantId, String secondParticipantId);

  /**
   * Оставляет запрос на деанонимизацию чата от пользователя с указанным идентификатором. Возвращает
   * {@link Optional} с новым неанонимным чатом, если другой собеседник уже подавал заявку.
   *
   * @param chatId идентификатор чата
   * @param senderId идентификатор пользователя, отправляющего запрос на деанонимизацию
   * @return {@link Optional} с новым неанонимным чатом, если другой собеседник уже отправлял запрос
   */
  Optional<Chat> agreeToDeanonymization(Long chatId, String senderId);

  /**
   * Закрывает чат от имени пользователя с указанным идентификатором.
   *
   * @param chatId идентификатор чата
   * @param participantId идентификатор пользователя, закрывающего чат
   * @return закрытый чат
   */
  Chat closeChat(Long chatId, String participantId);

  Collection<Chat> getChatsByUserId(String userId);
}
