package ru.maxizenit.backend.chatservice.service;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxizenit.backend.chatservice.entity.Chat;
import ru.maxizenit.backend.chatservice.exception.ChatIsAlreadyClosedException;
import ru.maxizenit.backend.chatservice.exception.ChatIsNotAnonymousException;
import ru.maxizenit.backend.chatservice.exception.ChatNotFoundException;
import ru.maxizenit.backend.chatservice.repository.ChatRepository;
import ru.maxizenit.backend.chatservice.util.UserInChatChecker;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatRepository chatRepository;
  private final UserInChatChecker userInChatChecker;

  public Chat getChatById(long chatId) {
    return chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));
  }

  public Chat createChat(String firstParticipantId, String secondParticipantId, boolean anonymous) {
    Chat chat = new Chat();
    chat.setFirstParticipantId(firstParticipantId);
    chat.setSecondParticipantId(secondParticipantId);
    chat.setAnonymous(anonymous);
    chat.setFirstParticipantAgreeToDeAnonymization(false);
    chat.setSecondParticipantAgreeToDeAnonymization(false);
    chat.setIsClosed(false);
    chat.setNewChatId(null);

    return chatRepository.save(chat);
  }

  public Chat closeAnonymousChat(long chatId, String requesterId) {
    Chat chat = getChatById(chatId);

    userInChatChecker.check(chat, requesterId);
    if (!chat.getAnonymous()) {
      throw new ChatIsNotAnonymousException(chatId);
    }
    if (chat.getIsClosed()) {
      throw new ChatIsAlreadyClosedException(chatId);
    }

    chat.setIsClosed(true);
    return chatRepository.save(chat);
  }

  public Chat agreeToDeAnonymization(long chatId, String requesterId) {
    Chat chat = getChatById(chatId);
    userInChatChecker.check(chat, requesterId);
    if (!chat.getAnonymous()) {
      throw new ChatIsNotAnonymousException(chatId);
    }
    if (chat.getIsClosed()) {
      throw new ChatIsAlreadyClosedException(chatId);
    }

    if (chat.getFirstParticipantId().equals(requesterId)) {
      chat.setFirstParticipantAgreeToDeAnonymization(true);
    } else {
      chat.setSecondParticipantAgreeToDeAnonymization(false);
    }

    if (chat.getFirstParticipantAgreeToDeAnonymization()
        && chat.getSecondParticipantAgreeToDeAnonymization()) {
      Chat newChat =
          getNonAnonymousChatByParticipantIds(
              chat.getFirstParticipantId(), chat.getSecondParticipantId());
      if (newChat == null) {
        newChat = createChat(chat.getFirstParticipantId(), chat.getSecondParticipantId(), false);
      }

      chat.setNewChatId(newChat.getNewChatId());
    }

    return chatRepository.save(chat);
  }

  public Chat getNonAnonymousChatByParticipantIds(
      String firstParticipantId, String secondParticipantId) {
    return chatRepository.findNonAnonymousChatByParticipantIds(
        firstParticipantId, secondParticipantId);
  }

  public Collection<Chat> findChatsByParticipantId(String partcipantId) {
    return chatRepository.findByParticipantId(partcipantId);
  }
}
