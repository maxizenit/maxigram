package ru.maxigram.backend.chatservice.grpc;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.grpc.stub.StreamObserver;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.lang3.StringUtils;
import ru.maxigram.backend.chatservice.entity.Chat;
import ru.maxigram.backend.chatservice.entity.Message;
import ru.maxigram.backend.chatservice.service.AnonymousChatQueue;
import ru.maxigram.backend.chatservice.service.ChatMessageFacade;
import ru.maxigram.backend.chatservice.service.ChatService;
import ru.maxigram.backend.grpccommons.ChatServiceGrpc;
import ru.maxigram.backend.grpccommons.ChatServiceOuterClass;

@GrpcService
@RequiredArgsConstructor
public class ChatServiceGrpcImpl extends ChatServiceGrpc.ChatServiceImplBase {

  private final ChatService chatService;
  private final ChatMessageFacade chatMessageFacade;
  private final AnonymousChatQueue anonymousChatQueue;

  @Override
  public void sendMessage(
      ChatServiceOuterClass.SendMessageRequest request,
      StreamObserver<ChatServiceOuterClass.Message> responseObserver) {
    Message message =
        chatMessageFacade.sendMessage(
            request.getSenderId(), request.getReceiverId(), request.getText());
    ChatServiceOuterClass.Message result =
        ChatServiceOuterClass.Message.newBuilder()
            .setId(message.getId())
            .setChatId(message.getChat().getId())
            .setTimestamp(Timestamps.fromMillis(message.getTimestamp().getTime()))
            .setSenderId(message.getSenderId())
            .setText(message.getText())
            .setRead(message.getRead())
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void sendAnonymousMessage(
      ChatServiceOuterClass.SendAnonymousMessageRequest request,
      StreamObserver<ChatServiceOuterClass.Message> responseObserver) {
    Message message =
        chatMessageFacade.sendMessage(
            request.getSenderId(), request.getChatId(), request.getText());
    ChatServiceOuterClass.Message result =
        ChatServiceOuterClass.Message.newBuilder()
            .setId(message.getId())
            .setChatId(message.getChat().getId())
            .setTimestamp(Timestamps.fromMillis(message.getTimestamp().getTime()))
            .setSenderId(message.getSenderId())
            .setText(message.getText())
            .setRead(message.getRead())
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void agreeToDeanonymization(
      ChatServiceOuterClass.AgreeToDeanonymizationRequest request,
      StreamObserver<ChatServiceOuterClass.AgreeToDeanonymizationResponse> responseObserver) {
    Optional<Chat> newChat =
        chatService.agreeToDeanonymization(request.getChatId(), request.getSenderId());
    ChatServiceOuterClass.AgreeToDeanonymizationResponse result =
        ChatServiceOuterClass.AgreeToDeanonymizationResponse.newBuilder()
            .setNewChatId(newChat.isPresent() ? newChat.get().getId() : -1)
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void closeChat(
      ChatServiceOuterClass.CloseChatRequest request,
      StreamObserver<ChatServiceOuterClass.Chat> responseObserver) {
    Chat chat = chatService.closeChat(request.getChatId(), request.getParticipantId());
    ChatServiceOuterClass.Chat result =
        ChatServiceOuterClass.Chat.newBuilder()
            .setId(chat.getId())
            .setFirstParticipantId(chat.getFirstParticipantId())
            .setSecondParticipantId(chat.getSecondParticipantId())
            .setAnonymous(chat.getAnonymous())
            .setFirstParticipantAgreesToDeanonymization(
                chat.getFirstParticipantAgreesToDeanonymization())
            .setSecondParticipantAgreesToDeanonymization(
                chat.getSecondParticipantAgreesToDeanonymization())
            .setIsClosed(chat.getIsClosed())
            .setParticipantClosedChatId(
                StringUtils.defaultString(chat.getParticipantClosedChatId()))
            .setNewChatId(Objects.nonNull(chat.getNewChatId()) ? chat.getNewChatId() : -1)
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void applyAnonymousChat(
      ChatServiceOuterClass.ApplyAnonymousChatRequest request,
      StreamObserver<ChatServiceOuterClass.ApplyAnonymousChatResponse> responseObserver) {
    anonymousChatQueue.addUserInQueue(request.getUserId());

    responseObserver.onNext(ChatServiceOuterClass.ApplyAnonymousChatResponse.newBuilder().build());
    responseObserver.onCompleted();
  }

  @Override
  public void getChats(
      ChatServiceOuterClass.GetChatsRequest request,
      StreamObserver<ChatServiceOuterClass.GetChatsResponse> responseObserver) {
    Collection<Chat> chats = chatService.getChatsByUserId(request.getUserId());
    Map<Long, Message> lastMessages =
        chatMessageFacade.getLastMessagesByChatIds(chats.stream().map(Chat::getId).toList());

    ChatServiceOuterClass.GetChatsResponse result =
        ChatServiceOuterClass.GetChatsResponse.newBuilder()
            .addAllChatCards(
                chats.stream()
                    .map(
                        c -> {
                          Message lastMessage = lastMessages.getOrDefault(c.getId(), null);
                          String lastMessageText = lastMessage != null ? lastMessage.getText() : "";
                          Timestamp lastMessageTimestamp =
                              lastMessage != null
                                  ? Timestamps.fromMillis(lastMessage.getTimestamp().getTime())
                                  : null;
                          boolean lastMessageRead =
                              lastMessage != null ? lastMessage.getRead() : false;

                          return ChatServiceOuterClass.GetChatsResponseElement.newBuilder()
                              .setChatId(c.getId())
                              .setLastMessage(lastMessageText)
                              .setLastMessageTimestamp(lastMessageTimestamp)
                              .setLastMessageRead(lastMessageRead)
                              .setOtherParticipantId(
                                  c.getAnonymous()
                                      ? ""
                                      : (c.getFirstParticipantId().equals(request.getUserId())
                                          ? c.getSecondParticipantId()
                                          : c.getFirstParticipantId()))
                              .build();
                        })
                    .toList())
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void getMessagesFromChat(
      ChatServiceOuterClass.GetMessagesFromChatRequest request,
      StreamObserver<ChatServiceOuterClass.GetMessagesFromChatResponse> responseObserver) {
    Collection<Message> messages =
        chatMessageFacade.getAllMessagesInChat(request.getChatId(), request.getUserId());
    Boolean isAnonymous = chatService.findChatById(request.getChatId()).get().getAnonymous();
    ChatServiceOuterClass.GetMessagesFromChatResponse result =
        ChatServiceOuterClass.GetMessagesFromChatResponse.newBuilder()
            .addAllMessages(
                messages.stream()
                    .map(
                        m ->
                            ChatServiceOuterClass.Message.newBuilder()
                                .setId(m.getId())
                                .setChatId(m.getChat().getId())
                                .setTimestamp(Timestamps.fromMillis(m.getTimestamp().getTime()))
                                .setSenderId(isAnonymous ? "" : m.getSenderId())
                                .setText(m.getText())
                                .setRead(m.getRead())
                                .build())
                    .toList())
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }
}
