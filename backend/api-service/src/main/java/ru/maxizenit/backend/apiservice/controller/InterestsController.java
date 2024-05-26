package ru.maxizenit.backend.apiservice.controller;

import java.util.Collection;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.maxigram.backend.grpccommons.UserServiceGrpc;
import ru.maxigram.backend.grpccommons.UserServiceOuterClass;
import ru.maxigram.backend.maxigramcommons.dto.Interest;

@RestController
@RequestMapping("/interests")
public class InterestsController {

  @GrpcClient("user-service")
  private UserServiceGrpc.UserServiceBlockingStub userService;

  @GetMapping
  public ResponseEntity<Collection<Interest>> getAllInterests() {
    UserServiceOuterClass.GetAllInterestsResponse response =
        userService.getAllInterests(
            UserServiceOuterClass.GetAllInterestsRequest.newBuilder().build());

    Collection<Interest> result =
        response.getInterestsList().stream()
            .map(
                responseElement ->
                    Interest.builder()
                        .id(responseElement.getId())
                        .name(responseElement.getName())
                        .build())
            .toList();

    return ResponseEntity.ok(result);
  }
}
