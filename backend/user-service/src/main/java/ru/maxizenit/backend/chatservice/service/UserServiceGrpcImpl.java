package ru.maxizenit.backend.chatservice.service;

import com.google.protobuf.util.Timestamps;
import io.grpc.stub.StreamObserver;
import java.util.Collection;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.maxigram.backend.grpccommons.UserServiceGrpc;
import ru.maxigram.backend.grpccommons.UserServiceOuterClass;
import ru.maxizenit.backend.chatservice.entity.Subscription;
import ru.maxizenit.backend.chatservice.entity.UserProfile;

@GrpcService
@RequiredArgsConstructor
public class UserServiceGrpcImpl
    extends UserServiceGrpc.UserServiceImplBase { // todo: дореализовать методы здесь

  private final UserProfileService userProfileService;
  private final InterestService interestService;
  private final UserServiceFacade userServiceFacade;

  @Override
  public void getUserById(
      UserServiceOuterClass.GetUserByIdRequest request,
      StreamObserver<UserServiceOuterClass.User> responseObserver) {
    UserProfile userProfile = userProfileService.getUserProfileById(request.getUserId());
    UserServiceOuterClass.User response =
        UserServiceOuterClass.User.newBuilder()
            .setId(userProfile.getId())
            .setFirstName(userProfile.getFirstName())
            .setLastName(userProfile.getLastName())
            .setBirthdateTimestamp(Timestamps.fromDate(userProfile.getBirthdate()))
            .addAllInterests(
                userProfile.getInterests().stream()
                    .map(
                        interest ->
                            UserServiceOuterClass.Interest.newBuilder()
                                .setId(interest.getId())
                                .setName(interest.getName())
                                .build())
                    .toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void createUser(
      UserServiceOuterClass.CreateOrUpdateUserRequest request,
      StreamObserver<UserServiceOuterClass.User> responseObserver) {
    UserProfile userProfile =
        userServiceFacade.createUserProfile(
            request.getId(),
            request.getFirstName(),
            request.getLastName(),
            new Date(Timestamps.toMillis(request.getBirthdateTimestamp())),
            request.getInterestsIdsList());
    UserServiceOuterClass.User response =
        UserServiceOuterClass.User.newBuilder()
            .setId(userProfile.getId())
            .setFirstName(userProfile.getFirstName())
            .setLastName(userProfile.getLastName())
            .setBirthdateTimestamp(Timestamps.fromDate(userProfile.getBirthdate()))
            .addAllInterests(
                userProfile.getInterests().stream()
                    .map(
                        interest ->
                            UserServiceOuterClass.Interest.newBuilder()
                                .setId(interest.getId())
                                .setName(interest.getName())
                                .build())
                    .toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void updateUser(
      UserServiceOuterClass.CreateOrUpdateUserRequest request,
      StreamObserver<UserServiceOuterClass.User> responseObserver) {
    UserProfile userProfile =
        userServiceFacade.updateUserProfile(
            request.getId(),
            request.getFirstName(),
            request.getLastName(),
            new Date(Timestamps.toMillis(request.getBirthdateTimestamp())),
            request.getInterestsIdsList());
    UserServiceOuterClass.User response =
        UserServiceOuterClass.User.newBuilder()
            .setId(userProfile.getId())
            .setFirstName(userProfile.getFirstName())
            .setLastName(userProfile.getLastName())
            .setBirthdateTimestamp(Timestamps.fromDate(userProfile.getBirthdate()))
            .addAllInterests(
                userProfile.getInterests().stream()
                    .map(
                        interest ->
                            UserServiceOuterClass.Interest.newBuilder()
                                .setId(interest.getId())
                                .setName(interest.getName())
                                .build())
                    .toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getAllUsers(
      UserServiceOuterClass.GetAllUsersRequest request,
      StreamObserver<UserServiceOuterClass.GetAllUsersResponse> responseObserver) {
    Collection<UserProfile> userProfiles = userProfileService.getAllUserProfiles();

    UserServiceOuterClass.GetAllUsersResponse response =
        UserServiceOuterClass.GetAllUsersResponse.newBuilder()
            .addAllUsers(
                userProfiles.stream()
                    .map(
                        userProfile ->
                            UserServiceOuterClass.User.newBuilder()
                                .setId(userProfile.getId())
                                .setFirstName(userProfile.getFirstName())
                                .setLastName(userProfile.getLastName())
                                .setBirthdateTimestamp(
                                    Timestamps.fromDate(userProfile.getBirthdate()))
                                .addAllInterests(
                                    userProfile.getInterests().stream()
                                        .map(
                                            interest ->
                                                UserServiceOuterClass.Interest.newBuilder()
                                                    .setId(interest.getId())
                                                    .setName(interest.getName())
                                                    .build())
                                        .toList())
                                .build())
                    .toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getSubscriptionsByUserId(
      UserServiceOuterClass.GetSubscriptionsByUserIdRequest request,
      StreamObserver<UserServiceOuterClass.GetSubscriptionByUserIdResponse> responseObserver) {
    Collection<Subscription> subscriptions =
        userProfileService.getSubscriptionsByUserId(request.getUserId());

    UserServiceOuterClass.GetSubscriptionByUserIdResponse response =
        UserServiceOuterClass.GetSubscriptionByUserIdResponse.newBuilder()
            .addAllSubscriptions(
                subscriptions.stream()
                    .map(
                        subscription ->
                            UserServiceOuterClass.Subscription.newBuilder()
                                .setSubscriberId(subscription.getSubscriberId())
                                .setAuthorId(subscription.getAuthorId())
                                .build())
                    .toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void subscribeOnUserById(
      UserServiceOuterClass.SubscribeOnUserByIdRequest request,
      StreamObserver<UserServiceOuterClass.Subscription> responseObserver) {
    Subscription subscription =
        userProfileService.subscribeOnUserById(request.getUserId(), request.getSubscriberId());

    UserServiceOuterClass.Subscription response =
        UserServiceOuterClass.Subscription.newBuilder()
            .setSubscriberId(subscription.getSubscriberId())
            .setAuthorId(subscription.getAuthorId())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getSelfRestraint(
      UserServiceOuterClass.GetSelfRestraintRequest request,
      StreamObserver<UserServiceOuterClass.SelfRestraint> responseObserver) {
    super.getSelfRestraint(request, responseObserver);
  }

  @Override
  public void addSelfRestraint(
      UserServiceOuterClass.AddSelfRestraintRequest request,
      StreamObserver<UserServiceOuterClass.SelfRestraint> responseObserver) {
    super.addSelfRestraint(request, responseObserver);
  }

  @Override
  public void getAllInterests(
      UserServiceOuterClass.GetAllInterestsRequest request,
      StreamObserver<UserServiceOuterClass.GetAllInterestsResponse> responseObserver) {
    UserServiceOuterClass.GetAllInterestsResponse response =
        UserServiceOuterClass.GetAllInterestsResponse.newBuilder()
            .addAllInterests(
                interestService.getAllInterests().stream()
                    .map(
                        interest ->
                            UserServiceOuterClass.Interest.newBuilder()
                                .setId(interest.getId())
                                .setName(interest.getName())
                                .build())
                    .toList())
            .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
