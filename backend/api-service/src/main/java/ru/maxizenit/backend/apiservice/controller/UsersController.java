package ru.maxizenit.backend.apiservice.controller;

import com.google.protobuf.util.Timestamps;
import io.grpc.StatusRuntimeException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.maxigram.backend.grpccommons.ChatServiceGrpc;
import ru.maxigram.backend.grpccommons.ChatServiceOuterClass;
import ru.maxigram.backend.grpccommons.UserServiceGrpc;
import ru.maxigram.backend.grpccommons.UserServiceOuterClass;
import ru.maxigram.backend.maxigramcommons.dto.*;
import ru.maxigram.backend.maxigramcommons.dto.request.CreateOrUpdateUserRequest;
import ru.maxigram.backend.maxigramcommons.dto.request.SendMessageRequest;

@RestController
@RequestMapping("/users")
public class UsersController {

  @GrpcClient("user-service")
  private UserServiceGrpc.UserServiceBlockingStub userService;

  @GrpcClient("chat-service")
  private ChatServiceGrpc.ChatServiceBlockingStub chatService;

  @GetMapping("/{id}")
  public ResponseEntity<User> getUserById(@PathVariable String id) {
    try {
      UserServiceOuterClass.User response =
          userService.getUserById(
              UserServiceOuterClass.GetUserByIdRequest.newBuilder().setUserId(id).build());

      User result =
          User.builder()
              .id(response.getId())
              .firstName(response.getFirstName())
              .lastName(response.getLastName())
              .birthdate(new Date(Timestamps.toMillis(response.getBirthdateTimestamp())))
              .interests(
                  response.getInterestsList().stream()
                      .map(
                          interestFromResponse ->
                              Interest.builder()
                                  .id(interestFromResponse.getId())
                                  .name(interestFromResponse.getName())
                                  .build())
                      .toList())
              .build();

      return ResponseEntity.ok(result);
    } catch (StatusRuntimeException e) {
      return ResponseEntity.ok(null);
    }
  }

  @PostMapping
  public ResponseEntity<User> createUser(@RequestBody CreateOrUpdateUserRequest request) {
    UserServiceOuterClass.User response =
        userService.createUser(
            UserServiceOuterClass.CreateOrUpdateUserRequest.newBuilder()
                .setId(request.getId())
                .setFirstName(request.getFirstName())
                .setLastName(request.getLastName())
                .setBirthdateTimestamp(Timestamps.fromMillis(request.getBirthdate().getTime()))
                .addAllInterestsIds(request.getInterestsIds())
                .build());

    User result =
        User.builder()
            .id(response.getId())
            .firstName(response.getFirstName())
            .lastName(response.getLastName())
            .birthdate(new Date(Timestamps.toMillis(response.getBirthdateTimestamp())))
            .interests(
                response.getInterestsList().stream()
                    .map(
                        interestFromResponse ->
                            Interest.builder()
                                .id(interestFromResponse.getId())
                                .name(interestFromResponse.getName())
                                .build())
                    .toList())
            .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @PutMapping
  public ResponseEntity<User> updateUser(@RequestBody CreateOrUpdateUserRequest request) {
    UserServiceOuterClass.User response =
        userService.updateUser(
            UserServiceOuterClass.CreateOrUpdateUserRequest.newBuilder()
                .setId(request.getId())
                .setFirstName(request.getFirstName())
                .setLastName(request.getLastName())
                .setBirthdateTimestamp(Timestamps.fromMillis(request.getBirthdate().getTime()))
                .addAllInterestsIds(request.getInterestsIds())
                .build());

    User result =
        User.builder()
            .id(response.getId())
            .firstName(response.getFirstName())
            .lastName(response.getLastName())
            .birthdate(new Date(Timestamps.toMillis(response.getBirthdateTimestamp())))
            .interests(
                response.getInterestsList().stream()
                    .map(
                        interestFromResponse ->
                            Interest.builder()
                                .id(interestFromResponse.getId())
                                .name(interestFromResponse.getName())
                                .build())
                    .toList())
            .build();

    return ResponseEntity.ok(result);
  }

  @GetMapping
  public ResponseEntity<Collection<User>> getAllUsers() {
    UserServiceOuterClass.GetAllUsersResponse response =
        userService.getAllUsers(UserServiceOuterClass.GetAllUsersRequest.newBuilder().build());

    Collection<User> result =
        response.getUsersList().stream()
            .map(
                responseElement ->
                    User.builder()
                        .id(responseElement.getId())
                        .firstName(responseElement.getFirstName())
                        .lastName(responseElement.getLastName())
                        .birthdate(
                            new Date(Timestamps.toMillis(responseElement.getBirthdateTimestamp())))
                        .build())
            .toList();

    return ResponseEntity.ok(result);
  }

  @GetMapping("/{id}/subscriptions")
  public ResponseEntity<Collection<Subscription>> getSubscriptionsByUserId(
      @PathVariable String id) {
    UserServiceOuterClass.GetSubscriptionByUserIdResponse response =
        userService.getSubscriptionsByUserId(
            UserServiceOuterClass.GetSubscriptionsByUserIdRequest.newBuilder()
                .setUserId(id)
                .build());

    Collection<Subscription> result =
        response.getSubscriptionsList().stream()
            .map(
                responseElement ->
                    Subscription.builder()
                        .subscriberId(responseElement.getSubscriberId())
                        .authorId(responseElement.getAuthorId())
                        .build())
            .toList();

    return ResponseEntity.ok(result);
  }

  @PostMapping("/{id}/subscribe")
  public ResponseEntity<Subscription> subscribeOnUserById(
      @PathVariable String id, @RequestParam String subscriberId) {
    UserServiceOuterClass.Subscription response =
        userService.subscribeOnUserById(
            UserServiceOuterClass.SubscribeOnUserByIdRequest.newBuilder()
                .setUserId(id)
                .setSubscriberId(subscriberId)
                .build());

    Subscription result =
        Subscription.builder()
            .subscriberId(response.getSubscriberId())
            .authorId(response.getAuthorId())
            .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping("/{id}/self-restraint")
  public ResponseEntity<SelfRestraint> getSelfRestraint(@PathVariable String id) {
    UserServiceOuterClass.SelfRestraint response =
        userService.getSelfRestraint(
            UserServiceOuterClass.GetSelfRestraintRequest.newBuilder().setUserId(id).build());

    SelfRestraint result =
        response.getNonNull()
            ? SelfRestraint.builder()
                .userId(response.getUserId())
                .startTime(new Timestamp(Timestamps.toMillis(response.getStartTime())))
                .endTime(new Timestamp(Timestamps.toMillis(response.getEndTime())))
                .build()
            : null;

    return ResponseEntity.ok(result);
  }

  @PostMapping("/self-restraint")
  public ResponseEntity<SelfRestraint> addSelfRestraint(@RequestBody SelfRestraint selfRestraint) {
    UserServiceOuterClass.SelfRestraint response =
        userService.addSelfRestraint(
            UserServiceOuterClass.AddSelfRestraintRequest.newBuilder()
                .setUserId(selfRestraint.getUserId())
                .setStartTime(Timestamps.fromMillis(selfRestraint.getStartTime().getTime()))
                .setEndTime(Timestamps.fromMillis(selfRestraint.getEndTime().getTime()))
                .build());

    SelfRestraint result =
        SelfRestraint.builder()
            .userId(response.getUserId())
            .startTime(new Timestamp(Timestamps.toMillis(response.getStartTime())))
            .endTime(new Timestamp(Timestamps.toMillis(response.getEndTime())))
            .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @PostMapping("/{id}/send-message")
  public ResponseEntity<Message> sendMessage(
      @PathVariable String id, @RequestBody SendMessageRequest request) {
    ChatServiceOuterClass.Message response =
        chatService.sendMessageToUser(
            ChatServiceOuterClass.SendMessageToUserRequest.newBuilder()
                .setSenderId(request.getSenderId())
                .setReceiverId(id)
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
}
