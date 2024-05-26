package ru.maxizenit.backend.chatservice.service;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.maxizenit.backend.chatservice.entity.Chat;
import ru.maxizenit.backend.chatservice.entity.Message;
import ru.maxizenit.backend.chatservice.exception.ChatNotFoundException;
import ru.maxizenit.backend.chatservice.util.UserInChatChecker;

@Service
@RequiredArgsConstructor
public class ChatServiceFacade {

  private final ChatService chatService;
  private final MessageService messageService;
  private final UserInChatChecker userInChatChecker;

  public Message sendMessageByChatId(String senderId, long chatId, String text) {
    Chat chat = chatService.getChatById(chatId);
    userInChatChecker.check(chat, senderId);

    return messageService.createMessage(chat, senderId, text);
  }

  @Transactional
  public Message sendMessageByReceiverId(String senderId, String receiverId, String text) {
    Chat chat;
    try {
      chat = chatService.getChatById(Long.parseLong(receiverId));
    } catch (ChatNotFoundException e) {
      chat = chatService.createChat(senderId, receiverId, false);
    }

    return messageService.createMessage(chat, senderId, text);
  }

  public Collection<Message> getMessagesByChatId(long chatId, String requesterId) {
    Chat chat = chatService.getChatById(chatId);
    userInChatChecker.check(chat, requesterId);
    return messageService.getMessagesOfChat(chat, requesterId);
  }
}
