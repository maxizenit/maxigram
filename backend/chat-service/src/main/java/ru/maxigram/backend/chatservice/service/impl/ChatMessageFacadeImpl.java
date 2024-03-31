package ru.maxigram.backend.chatservice.service.impl;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.maxigram.backend.chatservice.entity.Chat;
import ru.maxigram.backend.chatservice.entity.Message;
import ru.maxigram.backend.chatservice.exception.ChatIsClosedException;
import ru.maxigram.backend.chatservice.exception.ChatNotFoundException;
import ru.maxigram.backend.chatservice.exception.UserIsNotInChatException;
import ru.maxigram.backend.chatservice.service.ChatMessageFacade;
import ru.maxigram.backend.chatservice.service.ChatService;
import ru.maxigram.backend.chatservice.service.MessageService;
import ru.maxigram.backend.chatservice.util.UserInChatChecker;

@Service
@RequiredArgsConstructor
public class ChatMessageFacadeImpl implements ChatMessageFacade {

  private final ChatService chatService;
  private final MessageService messageService;
  private final UserInChatChecker userInChatChecker;

  @Override
  @Transactional
  public Message sendMessage(String senderId, String receiverId, String text) {
    Chat chat =
        chatService
            .findChatByParticipantIds(senderId, receiverId)
            .orElseGet(() -> chatService.createChat(senderId, receiverId, false));
    if (chat.getIsClosed()) {
      throw new ChatIsClosedException(chat.getId());
    }

    return messageService.createMessage(chat, senderId, text);
  }

  @Override
  @Transactional
  public Message sendMessage(String senderId, Long chatId, String text) {
    Chat chat =
        chatService.findChatById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));
    if (!userInChatChecker.check(chat, senderId)) {
      throw new UserIsNotInChatException(senderId, chat.getId());
    }
    if (chat.getIsClosed()) {
      throw new ChatIsClosedException(chat.getId());
    }

    return messageService.createMessage(chat, senderId, text);
  }

  @Override
  public Collection<Message> getAllMessagesInChat(Long chatId, String userId) {
    Chat chat =
        chatService.findChatById(chatId).orElseThrow(() -> new ChatNotFoundException(chatId));
    if (!userInChatChecker.check(chat, userId)) {
      throw new UserIsNotInChatException(userId, chat.getId());
    }

    Collection<Message> messages = messageService.getAllMessagesFromChat(chat);
    Collection<Message> unreadMessages =
        messages.stream().filter(m -> !m.getRead() && !m.getSenderId().equals(userId)).toList();
    if (!CollectionUtils.isEmpty(unreadMessages)) {
      unreadMessages.forEach(m -> m.setRead(true));
      messageService.saveAllMessages(unreadMessages);
    }

    return messages;
  }

  @Override
  public Map<Long, Message> getLastMessagesByChatIds(Collection<Long> chatIds) {
    return chatIds.stream()
        .collect(
            Collectors.toMap(
                id -> id,
                id -> {
                  Chat chat =
                      chatService.findChatById(id).orElseThrow(() -> new ChatNotFoundException(id));
                  return messageService.getLastMessageFromChat(chat);
                }));
  }
}
