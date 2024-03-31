package ru.maxigram.backend.userservice.grpc;

import com.google.protobuf.util.Timestamps;
import io.grpc.stub.StreamObserver;
import java.util.Collection;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.maxigram.backend.grpccommons.UserServiceGrpc;
import ru.maxigram.backend.grpccommons.UserServiceOuterClass;
import ru.maxigram.backend.userservice.entity.Interest;
import ru.maxigram.backend.userservice.entity.UserProfile;
import ru.maxigram.backend.userservice.exception.UserProfileNotFoundException;
import ru.maxigram.backend.userservice.service.UserProfileInterestFacade;
import ru.maxigram.backend.userservice.service.UserProfileService;

@GrpcService
@RequiredArgsConstructor
public class UserServiceGrpcImpl extends UserServiceGrpc.UserServiceImplBase {

  private final UserProfileService userProfileService;
  private final UserProfileInterestFacade userProfileInterestFacade;

  @Override
  public void createUserProfile(
      UserServiceOuterClass.UserProfile request,
      StreamObserver<UserServiceOuterClass.UserProfile> responseObserver) {
    UserProfile userProfile =
        userProfileService.createUserProfile(
            request.getUserId(),
            new Date(Timestamps.toMillis(request.getBirthdate())),
            request.getFirstName(),
            request.getLastName());
    UserServiceOuterClass.UserProfile result =
        UserServiceOuterClass.UserProfile.newBuilder()
            .setUserId(userProfile.getUserId())
            .setBirthdate(Timestamps.fromMillis(userProfile.getBirthdate().getTime()))
            .setFirstName(userProfile.getFirstName())
            .setLastName(userProfile.getLastName())
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void getUserProfile(
      UserServiceOuterClass.GetUserProfileRequest request,
      StreamObserver<UserServiceOuterClass.UserProfile> responseObserver) {
    UserProfile userProfile =
        userProfileService
            .findUserProfileById(request.getUserId())
            .orElseThrow(() -> new UserProfileNotFoundException(request.getUserId()));
    UserServiceOuterClass.UserProfile result =
        UserServiceOuterClass.UserProfile.newBuilder()
            .setUserId(userProfile.getUserId())
            .setBirthdate(Timestamps.fromMillis(userProfile.getBirthdate().getTime()))
            .setFirstName(userProfile.getFirstName())
            .setLastName(userProfile.getLastName())
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void getUserInterests(
      UserServiceOuterClass.GetUserInterestsRequest request,
      StreamObserver<UserServiceOuterClass.GetUserInterestsResponse> responseObserver) {
    Collection<Interest> interests =
        userProfileService
            .findUserProfileById(request.getUserId())
            .orElseThrow(() -> new UserProfileNotFoundException(request.getUserId()))
            .getInterests();
    UserServiceOuterClass.GetUserInterestsResponse result =
        UserServiceOuterClass.GetUserInterestsResponse.newBuilder()
            .addAllInterests(
                interests.stream()
                    .map(
                        i ->
                            UserServiceOuterClass.Interest.newBuilder()
                                .setId(i.getId())
                                .setName(i.getName())
                                .build())
                    .toList())
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void getAllUserProfiles(
      UserServiceOuterClass.GetAllUserProfilesRequest request,
      StreamObserver<UserServiceOuterClass.GetAllUserProfilesResponse> responseObserver) {
    Collection<UserProfile> userProfiles = userProfileService.getAllUserProfiles();
    UserServiceOuterClass.GetAllUserProfilesResponse result =
        UserServiceOuterClass.GetAllUserProfilesResponse.newBuilder()
            .addAllProfiles(
                userProfiles.stream()
                    .map(
                        up ->
                            UserServiceOuterClass.UserProfile.newBuilder()
                                .setUserId(up.getUserId())
                                .setBirthdate(Timestamps.fromMillis(up.getBirthdate().getTime()))
                                .setFirstName(up.getFirstName())
                                .setLastName(up.getLastName())
                                .build())
                    .toList())
            .build();

    responseObserver.onNext(result);
    responseObserver.onCompleted();
  }

  @Override
  public void addInterestToUserProfile(
      UserServiceOuterClass.AddInterestToUserProfileRequest request,
      StreamObserver<UserServiceOuterClass.AddInterestToUserProfileResponse> responseObserver) {
    userProfileInterestFacade.addInterestToUserProfile(
        request.getUserId(), request.getInterestId());

    responseObserver.onCompleted();
  }
}
