package ru.maxizenit.backend.apiservice.service;

import com.google.protobuf.util.Timestamps;
import io.micrometer.common.util.StringUtils;
import java.sql.Timestamp;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.maxigram.backend.apicommons.dto.ChatListElement;
import ru.maxigram.backend.apicommons.dto.Message;
import ru.maxigram.backend.apicommons.dto.UserProfile;
import ru.maxigram.backend.grpccommons.ChatServiceGrpc;
import ru.maxigram.backend.grpccommons.ChatServiceOuterClass;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final UserService userService;

  @GrpcClient("chat-service")
  private ChatServiceGrpc.ChatServiceBlockingStub chatStub;

  public Collection<Message> getMessagesFromChat(Long chatId, String userId) {
    return chatStub
        .getMessagesFromChat(
            ChatServiceOuterClass.GetMessagesFromChatRequest.newBuilder()
                .setChatId(chatId)
                .setUserId(userId)
                .build())
        .getMessagesList()
        .stream()
        .map(
            m ->
                Message.builder()
                    .id(m.getId())
                    .chatId(m.getChatId())
                    .timestamp(new Timestamp(Timestamps.toMillis(m.getTimestamp())))
                    .senderId(m.getSenderId())
                    .text(m.getText())
                    .read(m.getRead())
                    .build())
        .toList();
  }

  public Collection<ChatListElement> getChatsByUserId(String userId) {
    return chatStub
        .getChats(ChatServiceOuterClass.GetChatsRequest.newBuilder().setUserId(userId).build())
        .getChatCardsList()
        .stream()
        .map(
            cc -> {
              String otherParticipantFirstName = "";
              String otherParticipantLastName = "";
              if (!StringUtils.isBlank(cc.getOtherParticipantId())) {
                UserProfile userProfile =
                    userService.getUserProfileById(cc.getOtherParticipantId());
                otherParticipantFirstName = userProfile.getFirstName();
                otherParticipantLastName = userProfile.getLastName();
              }

              return ChatListElement.builder()
                  .chatId(cc.getChatId())
                  .lastMessage(cc.getLastMessage())
                  .lastMessageTimestamp(
                      new Timestamp(Timestamps.toMillis(cc.getLastMessageTimestamp())))
                  .read(cc.getLastMessageRead())
                  .otherParticipantFirstName(otherParticipantFirstName)
                  .otherParticipantLastName(otherParticipantLastName)
                  .build();
            })
        .toList();
  }
}
