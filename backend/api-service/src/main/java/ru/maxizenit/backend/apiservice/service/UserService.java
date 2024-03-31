package ru.maxizenit.backend.apiservice.service;

import com.google.protobuf.util.Timestamps;
import java.sql.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.maxigram.backend.apicommons.dto.UserProfile;
import ru.maxigram.backend.grpccommons.UserServiceGrpc;
import ru.maxigram.backend.grpccommons.UserServiceOuterClass;

@Service
public class UserService {

  @GrpcClient("user-service")
  private UserServiceGrpc.UserServiceBlockingStub userStub;

  public UserProfile getUserProfileById(String id) {
    UserServiceOuterClass.UserProfile result =
        userStub.getUserProfile(
            UserServiceOuterClass.GetUserProfileRequest.newBuilder().setUserId(id).build());

    return UserProfile.builder()
        .id(result.getUserId())
        .birthdate(new Timestamp(Timestamps.toMillis(result.getBirthdate())))
        .firstName(result.getFirstName())
        .lastName(result.getLastName())
        .build();
  }
}
