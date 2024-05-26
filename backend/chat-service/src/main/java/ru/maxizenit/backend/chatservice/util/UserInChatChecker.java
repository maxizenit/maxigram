package ru.maxizenit.backend.chatservice.util;

import org.springframework.stereotype.Component;
import ru.maxizenit.backend.chatservice.entity.Chat;
import ru.maxizenit.backend.chatservice.exception.UserIsNotInChatException;

@Component
public class UserInChatChecker {

  public void check(Chat chat, String userId) {
    if (!(chat.getFirstParticipantId().equals(userId)
        || chat.getSecondParticipantId().equals(userId))) {
      throw new UserIsNotInChatException(userId, chat.getId());
    }
  }
}
