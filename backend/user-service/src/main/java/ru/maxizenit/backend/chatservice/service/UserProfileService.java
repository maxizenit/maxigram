package ru.maxizenit.backend.chatservice.service;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxizenit.backend.chatservice.entity.Interest;
import ru.maxizenit.backend.chatservice.entity.Subscription;
import ru.maxizenit.backend.chatservice.entity.UserProfile;
import ru.maxizenit.backend.chatservice.exception.UserProfileNotFoundException;
import ru.maxizenit.backend.chatservice.repository.UserProfileRepository;

@Service
@RequiredArgsConstructor
public class UserProfileService {

  private final UserProfileRepository userProfileRepository;

  public UserProfile getUserProfileById(String id) {
    return userProfileRepository
        .findById(id)
        .orElseThrow(() -> new UserProfileNotFoundException(id));
  }

  public UserProfile createUserProfile(
      String id,
      String firstName,
      String lastName,
      Date birthdate,
      Collection<Interest> interests) {
    UserProfile userProfile = new UserProfile();
    userProfile.setId(id);
    userProfile.setFirstName(firstName);
    userProfile.setLastName(lastName);
    userProfile.setBirthdate(birthdate);
    userProfile.setInterests(interests);

    return userProfileRepository.save(userProfile);
  }

  public UserProfile updateUserProfile(
      String id,
      String firstName,
      String lastName,
      Date birthdate,
      Collection<Interest> interests) {
    UserProfile userProfile = getUserProfileById(id);
    userProfile.setFirstName(firstName);
    userProfile.setLastName(lastName);
    userProfile.setBirthdate(birthdate);
    userProfile.setInterests(interests);

    return userProfileRepository.save(userProfile);
  }

  public Collection<UserProfile> getAllUserProfiles() {
    return (Collection<UserProfile>) userProfileRepository.findAll();
  }

  public Collection<Subscription> getSubscriptionsByUserId(String id) {
    return getUserProfileById(id).getSubscriptions();
  }

  public Subscription subscribeOnUserById(String id, String subscriberId) {
    UserProfile userProfile = getUserProfileById(id);
    Optional<Subscription> existingSubscription =
        userProfile.getSubscriptions().stream()
            .filter(s -> s.getAuthorId().equals(id) && s.getSubscriberId().equals(subscriberId))
            .findAny();

    if (existingSubscription.isPresent()) {
      return existingSubscription.get();
    } else {
      Subscription subscription = new Subscription();
      subscription.setAuthorId(id);
      subscription.setSubscriberId(subscriberId);
      userProfile.getSubscriptions().add(subscription);

      userProfileRepository.save(userProfile);
      return subscription;
    }
  }
}
