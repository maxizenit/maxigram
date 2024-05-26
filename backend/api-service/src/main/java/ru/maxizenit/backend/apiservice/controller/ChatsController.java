package ru.maxizenit.backend.apiservice.controller;

import com.google.protobuf.util.Timestamps;
import java.sql.Timestamp;
import java.util.Collection;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.maxigram.backend.grpccommons.ChatServiceGrpc;
import ru.maxigram.backend.grpccommons.ChatServiceOuterClass;
import ru.maxigram.backend.maxigramcommons.dto.Chat;
import ru.maxigram.backend.maxigramcommons.dto.Message;
import ru.maxigram.backend.maxigramcommons.dto.request.SendMessageRequest;

@RestController
@RequestMapping("/chats")
public class ChatsController {

  @GrpcClient("chat-service")
  private ChatServiceGrpc.ChatServiceBlockingStub chatService;

  @GetMapping
  public ResponseEntity<Collection<Chat>> getAllChatsByUserId(@RequestParam String userId) {
    ChatServiceOuterClass.GetAllChatsByUserIdResponse response =
        chatService.getAllChatsByUserId(
            ChatServiceOuterClass.GetAllChatsByUserIdRequest.newBuilder()
                .setUserId(userId)
                .build());
    Collection<Chat> result =
        response.getChatsList().stream()
            .map(
                responseElement ->
                    Chat.builder()
                        .id(responseElement.getId())
                        .firstParticipantId(responseElement.getFirstParticipantId())
                        .secondParticipantId(responseElement.getSecondParticipantId())
                        .anonymous(responseElement.getAnonymous())
                        .firstParticipantAgreeToDeAnonymization(
                            responseElement.getFirstParticipantAgreeToDeAnonymization())
                        .secondParticipantAgreeToDeAnonymization(
                            responseElement.getSecondParticipantAgreeToDeAnonymization())
                        .isClosed(responseElement.getIsClosed())
                        .newChatId(responseElement.getNewChatId())
                        .lastMessage(responseElement.getLastMessage())
                        .build())
            .toList();
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Chat> getChatById(@PathVariable Long id, @RequestParam String requesterId) {
    ChatServiceOuterClass.Chat response =
        chatService.getChatById(
            ChatServiceOuterClass.GetChatByIdRequest.newBuilder()
                .setChatId(id)
                .setRequesterId(requesterId)
                .build());

    Chat result =
        Chat.builder()
            .id(response.getId())
            .firstParticipantId(response.getFirstParticipantId())
            .secondParticipantId(response.getSecondParticipantId())
            .anonymous(response.getAnonymous())
            .firstParticipantAgreeToDeAnonymization(
                response.getFirstParticipantAgreeToDeAnonymization())
            .secondParticipantAgreeToDeAnonymization(
                response.getSecondParticipantAgreeToDeAnonymization())
            .isClosed(response.getIsClosed())
            .newChatId(response.getNewChatId())
            .lastMessage(response.getLastMessage())
            .build();

    return ResponseEntity.ok(result);
  }

  @GetMapping("/{id}/messages")
  public ResponseEntity<Collection<Message>> getMessagesByChatId(
      @PathVariable Long id, @RequestParam String requesterId) {
    ChatServiceOuterClass.GetMessagesByChatIdResponse response =
        chatService.getMessagesByChatId(
            ChatServiceOuterClass.GetMessagesByChatIdRequest.newBuilder()
                .setChatId(id)
                .setRequesterId(requesterId)
                .build());
    Collection<Message> result =
        response.getMessagesList().stream()
            .map(
                responseElement ->
                    Message.builder()
                        .id(responseElement.getId())
                        .chatId(responseElement.getChatId())
                        .senderId(responseElement.getSenderId())
                        .text(responseElement.getText())
                        .timestamp(
                            new Timestamp(Timestamps.toMillis(responseElement.getTimestamp())))
                        .read(responseElement.getRead())
                        .build())
            .toList();
    return ResponseEntity.ok(result);
  }

  @PostMapping("/{id}/messages")
  public ResponseEntity<Message> sendMessageInChat(
      @PathVariable Long id, @RequestBody SendMessageRequest request) {
    ChatServiceOuterClass.Message response =
        chatService.sendMessageInChat(
            ChatServiceOuterClass.SendMessageInChatRequest.newBuilder()
                .setChatId(id)
                .setSenderId(request.getSenderId())
                .setText(request.getText())
                .build());
    Message result =
        Message.builder()
            .id(response.getId())
            .chatId(response.getChatId())
            .senderId(response.getSenderId())
            .text(response.getText())
            .timestamp(new Timestamp(Timestamps.toMillis(response.getTimestamp())))
            .read(response.getRead())
            .build();
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @PostMapping("/{id}/close")
  public ResponseEntity<Chat> closeAnonymousChat(
      @PathVariable Long id, @RequestParam String requesterId) {
    ChatServiceOuterClass.Chat response =
        chatService.closeAnonymousChat(
            ChatServiceOuterClass.CloseAnonymousChatRequest.newBuilder()
                .setChatId(id)
                .setRequesterId(requesterId)
                .build());
    Chat result =
        Chat.builder()
            .id(response.getId())
            .firstParticipantId(response.getFirstParticipantId())
            .secondParticipantId(response.getSecondParticipantId())
            .anonymous(response.getAnonymous())
            .firstParticipantAgreeToDeAnonymization(
                response.getFirstParticipantAgreeToDeAnonymization())
            .secondParticipantAgreeToDeAnonymization(
                response.getSecondParticipantAgreeToDeAnonymization())
            .isClosed(response.getIsClosed())
            .newChatId(response.getNewChatId())
            .lastMessage(response.getLastMessage())
            .build();
    return ResponseEntity.ok(result);
  }

  @PostMapping("/{id}/agree-to-de-anonymization")
  public ResponseEntity<Chat> agreeToDeAnonymization(
      @PathVariable Long id, @RequestParam String requesterId) {
    ChatServiceOuterClass.Chat response =
        chatService.agreeToDeAnonymization(
            ChatServiceOuterClass.AgreeToDeAnonymizationRequest.newBuilder()
                .setChatId(id)
                .setRequesterId(requesterId)
                .build());
    Chat result =
        Chat.builder()
            .id(response.getId())
            .firstParticipantId(response.getFirstParticipantId())
            .secondParticipantId(response.getSecondParticipantId())
            .anonymous(response.getAnonymous())
            .firstParticipantAgreeToDeAnonymization(
                response.getFirstParticipantAgreeToDeAnonymization())
            .secondParticipantAgreeToDeAnonymization(
                response.getSecondParticipantAgreeToDeAnonymization())
            .isClosed(response.getIsClosed())
            .newChatId(response.getNewChatId())
            .lastMessage(response.getLastMessage())
            .build();
    return ResponseEntity.ok(result);
  }

  @PostMapping("/request-for-anonymous-chat")
  public ResponseEntity<?> sendRequestForAnonymousChat(@RequestParam String requesterId) {
    chatService.sendRequestForAnonymousChat(
        ChatServiceOuterClass.SendRequestForAnonymousChatRequest.newBuilder()
            .setRequesterId(requesterId)
            .build());
    return ResponseEntity.ok(null);
  }
}
