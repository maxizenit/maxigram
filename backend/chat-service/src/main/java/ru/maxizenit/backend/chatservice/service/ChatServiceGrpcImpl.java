package ru.maxizenit.backend.chatservice.service;

import com.google.protobuf.util.Timestamps;
import io.grpc.stub.StreamObserver;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.maxigram.backend.grpccommons.ChatServiceGrpc;
import ru.maxigram.backend.grpccommons.ChatServiceOuterClass;
import ru.maxizenit.backend.chatservice.entity.Chat;
import ru.maxizenit.backend.chatservice.entity.Message;

@GrpcService
@RequiredArgsConstructor
public class ChatServiceGrpcImpl extends ChatServiceGrpc.ChatServiceImplBase {

  private final ChatService chatService;
  private final MessageService messageService;
  private final ChatServiceFacade chatServiceFacade;
  private final AnonymousChatQueue anonymousChatQueue;

  @Override
  public void getAllChatsByUserId(
      ChatServiceOuterClass.GetAllChatsByUserIdRequest request,
      StreamObserver<ChatServiceOuterClass.GetAllChatsByUserIdResponse> responseObserver) {
    Collection<Chat> chats = chatService.findChatsByParticipantId(request.getUserId());
    ChatServiceOuterClass.GetAllChatsByUserIdResponse response =
        ChatServiceOuterClass.GetAllChatsByUserIdResponse.newBuilder()
            .addAllChats(
                chats.stream()
                    .map(
                        c -> {
                          Message lastMessage = messageService.getLastMessageOfChat(c);
                          String firstParticipantId =
                              c.getAnonymous()
                                  ? (c.getFirstParticipantId().equals(request.getUserId())
                                      ? c.getFirstParticipantId()
                                      : "")
                                  : c.getFirstParticipantId();
                          String secondParticipantId =
                              c.getAnonymous()
                                  ? (c.getSecondParticipantId().equals(request.getUserId())
                                      ? c.getSecondParticipantId()
                                      : "")
                                  : c.getSecondParticipantId();
                          return ChatServiceOuterClass.Chat.newBuilder()
                              .setId(c.getId())
                              .setFirstParticipantId(firstParticipantId)
                              .setSecondParticipantId(secondParticipantId)
                              .setAnonymous(c.getAnonymous())
                              .setFirstParticipantAgreeToDeAnonymization(
                                  c.getFirstParticipantAgreeToDeAnonymization())
                              .setSecondParticipantAgreeToDeAnonymization(
                                  c.getSecondParticipantAgreeToDeAnonymization())
                              .setIsClosed(c.getIsClosed())
                              .setNewChatId(c.getNewChatId() != null ? c.getNewChatId() : -1)
                              .setLastMessage(lastMessage != null ? lastMessage.getText() : "")
                              .build();
                        })
                    .toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getChatById(
      ChatServiceOuterClass.GetChatByIdRequest request,
      StreamObserver<ChatServiceOuterClass.Chat> responseObserver) {
    Chat c = chatService.getChatById(request.getChatId());

    Message lastMessage = messageService.getLastMessageOfChat(c);
    String firstParticipantId =
        c.getAnonymous()
            ? (c.getFirstParticipantId().equals(request.getRequesterId())
                ? c.getFirstParticipantId()
                : "")
            : c.getFirstParticipantId();
    String secondParticipantId =
        c.getAnonymous()
            ? (c.getSecondParticipantId().equals(request.getRequesterId())
                ? c.getSecondParticipantId()
                : "")
            : c.getSecondParticipantId();
    ChatServiceOuterClass.Chat response =
        ChatServiceOuterClass.Chat.newBuilder()
            .setId(c.getId())
            .setFirstParticipantId(firstParticipantId)
            .setSecondParticipantId(secondParticipantId)
            .setAnonymous(c.getAnonymous())
            .setFirstParticipantAgreeToDeAnonymization(
                c.getFirstParticipantAgreeToDeAnonymization())
            .setSecondParticipantAgreeToDeAnonymization(
                c.getSecondParticipantAgreeToDeAnonymization())
            .setIsClosed(c.getIsClosed())
            .setNewChatId(c.getNewChatId() != null ? c.getNewChatId() : -1)
            .setLastMessage(lastMessage != null ? lastMessage.getText() : "")
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getMessagesByChatId(
      ChatServiceOuterClass.GetMessagesByChatIdRequest request,
      StreamObserver<ChatServiceOuterClass.GetMessagesByChatIdResponse> responseObserver) {
    Collection<Message> messages =
        chatServiceFacade.getMessagesByChatId(request.getChatId(), request.getRequesterId());
    ChatServiceOuterClass.GetMessagesByChatIdResponse response =
        ChatServiceOuterClass.GetMessagesByChatIdResponse.newBuilder()
            .addAllMessages(
                messages.stream()
                    .map(
                        m ->
                            ChatServiceOuterClass.Message.newBuilder()
                                .setId(m.getId())
                                .setChatId(m.getChat().getId())
                                .setSenderId(m.getSenderId())
                                .setText(m.getText())
                                .setTimestamp(Timestamps.fromMillis(m.getTimestamp().getTime()))
                                .setRead(m.getRead())
                                .build())
                    .toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void sendMessageInChat(
      ChatServiceOuterClass.SendMessageInChatRequest request,
      StreamObserver<ChatServiceOuterClass.Message> responseObserver) {
    Message message =
        chatServiceFacade.sendMessageByChatId(
            request.getSenderId(), request.getChatId(), request.getText());
    ChatServiceOuterClass.Message response =
        ChatServiceOuterClass.Message.newBuilder()
            .setId(message.getId())
            .setChatId(message.getChat().getId())
            .setSenderId(
                message.getChat().getAnonymous()
                    ? (request.getSenderId().equals(request.getSenderId())
                        ? message.getSenderId()
                        : "")
                    : request.getSenderId())
            .setText(message.getText())
            .setTimestamp(Timestamps.fromMillis(message.getTimestamp().getTime()))
            .setRead(message.getRead())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void closeAnonymousChat(
      ChatServiceOuterClass.CloseAnonymousChatRequest request,
      StreamObserver<ChatServiceOuterClass.Chat> responseObserver) {
    Chat chat = chatService.closeAnonymousChat(request.getChatId(), request.getRequesterId());
    Message lastMessage = messageService.getLastMessageOfChat(chat);
    String firstParticipantId =
        chat.getFirstParticipantId().equals(request.getRequesterId())
            ? chat.getFirstParticipantId()
            : "";
    String secondParticipantId =
        chat.getSecondParticipantId().equals(request.getRequesterId())
            ? chat.getSecondParticipantId()
            : "";

    ChatServiceOuterClass.Chat response =
        ChatServiceOuterClass.Chat.newBuilder()
            .setId(chat.getId())
            .setFirstParticipantId(firstParticipantId)
            .setSecondParticipantId(secondParticipantId)
            .setAnonymous(chat.getAnonymous())
            .setFirstParticipantAgreeToDeAnonymization(
                chat.getFirstParticipantAgreeToDeAnonymization())
            .setSecondParticipantAgreeToDeAnonymization(
                chat.getSecondParticipantAgreeToDeAnonymization())
            .setIsClosed(chat.getIsClosed())
            .setNewChatId(chat.getNewChatId() != null ? chat.getNewChatId() : -1)
            .setLastMessage(lastMessage != null ? lastMessage.getText() : "")
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void agreeToDeAnonymization(
      ChatServiceOuterClass.AgreeToDeAnonymizationRequest request,
      StreamObserver<ChatServiceOuterClass.Chat> responseObserver) {
    Chat chat = chatService.agreeToDeAnonymization(request.getChatId(), request.getRequesterId());
    Message lastMessage = messageService.getLastMessageOfChat(chat);
    String firstParticipantId =
        chat.getFirstParticipantId().equals(request.getRequesterId())
            ? chat.getFirstParticipantId()
            : "";
    String secondParticipantId =
        chat.getSecondParticipantId().equals(request.getRequesterId())
            ? chat.getSecondParticipantId()
            : "";

    ChatServiceOuterClass.Chat response =
        ChatServiceOuterClass.Chat.newBuilder()
            .setId(chat.getId())
            .setFirstParticipantId(firstParticipantId)
            .setSecondParticipantId(secondParticipantId)
            .setAnonymous(chat.getAnonymous())
            .setFirstParticipantAgreeToDeAnonymization(
                chat.getFirstParticipantAgreeToDeAnonymization())
            .setSecondParticipantAgreeToDeAnonymization(
                chat.getSecondParticipantAgreeToDeAnonymization())
            .setIsClosed(chat.getIsClosed())
            .setNewChatId(chat.getNewChatId() != null ? chat.getNewChatId() : -1)
            .setLastMessage(lastMessage != null ? lastMessage.getText() : "")
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void sendRequestForAnonymousChat(
      ChatServiceOuterClass.SendRequestForAnonymousChatRequest request,
      StreamObserver<ChatServiceOuterClass.Empty> responseObserver) {
    anonymousChatQueue.addUserInQueue(request.getRequesterId());

    responseObserver.onNext(ChatServiceOuterClass.Empty.newBuilder().build());
    responseObserver.onCompleted();
  }

  @Override
  public void sendMessageToUser(
      ChatServiceOuterClass.SendMessageToUserRequest request,
      StreamObserver<ChatServiceOuterClass.Message> responseObserver) {
    Message message =
        chatServiceFacade.sendMessageByReceiverId(
            request.getSenderId(), request.getReceiverId(), request.getText());
    ChatServiceOuterClass.Message response =
        ChatServiceOuterClass.Message.newBuilder()
            .setId(message.getId())
            .setChatId(message.getChat().getId())
            .setSenderId(request.getSenderId())
            .setText(message.getText())
            .setTimestamp(Timestamps.fromMillis(message.getTimestamp().getTime()))
            .setRead(message.getRead())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
