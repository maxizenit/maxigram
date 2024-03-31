package ru.maxigram.backend.chatservice.util;

import org.springframework.stereotype.Component;
import ru.maxigram.backend.chatservice.entity.Chat;

/** Класс для проверки принадлежности чата пользователю. */
@Component
public class UserInChatChecker {

  /**
   * Возвращает {@code true}, если пользователь есть в чате.
   *
   * @param chat чат
   * @param userId идентификатор пользователя
   * @return {@code true}, если пользователь есть в чате
   */
  public boolean check(Chat chat, String userId) {
    return chat.getFirstParticipantId().equals(userId)
        || chat.getSecondParticipantId().equals(userId);
  }
}
