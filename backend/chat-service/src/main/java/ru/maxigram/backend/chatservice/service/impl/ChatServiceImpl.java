package ru.maxigram.backend.chatservice.service.impl;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.maxigram.backend.chatservice.entity.Chat;
import ru.maxigram.backend.chatservice.exception.ChatIsAlreadyClosedException;
import ru.maxigram.backend.chatservice.exception.ChatNotFoundException;
import ru.maxigram.backend.chatservice.exception.UserIsNotInChatException;
import ru.maxigram.backend.chatservice.repository.ChatRepository;
import ru.maxigram.backend.chatservice.service.ChatService;
import ru.maxigram.backend.chatservice.util.UserInChatChecker;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

  private final ChatRepository chatRepository;
  private final UserInChatChecker userInChatChecker;

  @Override
  @Transactional
  public Chat createChat(String firstParticipantId, String secondParticipantId, Boolean anonymous) {
    Chat chat = new Chat();
    chat.setFirstParticipantId(firstParticipantId);
    chat.setSecondParticipantId(secondParticipantId);
    chat.setAnonymous(anonymous);
    chat.setCreationTimestamp(new Timestamp(System.currentTimeMillis()));
    chat.setFirstParticipantAgreesToDeanonymization(false);
    chat.setSecondParticipantAgreesToDeanonymization(false);
    chat.setIsClosed(false);

    return chatRepository.save(chat);
  }

  @Override
  public Optional<Chat> findChatById(Long id) {
    return chatRepository.findById(id);
  }

  @Override
  public Optional<Chat> findChatByParticipantIds(
      String firstParticipantId, String secondParticipantId) {
    return chatRepository.findByParticipantIds(firstParticipantId, secondParticipantId);
  }

  @Override
  @Transactional
  public Optional<Chat> agreeToDeanonymization(Long chatId, String senderId) {
    Chat chat = findChatById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));
    if (!userInChatChecker.check(chat, senderId)) {
      throw new UserIsNotInChatException(senderId, chat.getId());
    }

    String otherParticipantId;

    if (chat.getFirstParticipantId().equals(senderId)) {
      chat.setFirstParticipantAgreesToDeanonymization(true);
      otherParticipantId = chat.getSecondParticipantId();
    } else if (chat.getSecondParticipantId().equals(senderId)) {
      chat.setSecondParticipantAgreesToDeanonymization(true);
      otherParticipantId = chat.getFirstParticipantId();
    } else {
      throw new UserIsNotInChatException(senderId, chatId);
    }

    Chat nonAnonymousChat = null;
    if (chat.getFirstParticipantAgreesToDeanonymization()
        && chat.getSecondParticipantAgreesToDeanonymization()) {
      chat.setIsClosed(true);

      nonAnonymousChat =
          findChatByParticipantIds(senderId, otherParticipantId)
              .orElseGet(() -> createChat(senderId, otherParticipantId, false));
    }

    chatRepository.save(chat);

    return Objects.isNull(nonAnonymousChat) ? Optional.empty() : Optional.of(nonAnonymousChat);
  }

  @Override
  public Chat closeChat(Long chatId, String participantId) {
    Chat chat = findChatById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));
    if (!userInChatChecker.check(chat, participantId)) {
      throw new UserIsNotInChatException(participantId, chat.getId());
    }

    if (!chat.getIsClosed()) {
      chat.setIsClosed(true);
      chat.setParticipantClosedChatId(participantId);

      chatRepository.save(chat);
    } else {
      throw new ChatIsAlreadyClosedException(chatId);
    }

    return chat;
  }

  @Override
  public Collection<Chat> getChatsByUserId(String userId) {
    return chatRepository.findAllByUserId(userId);
  }
}
