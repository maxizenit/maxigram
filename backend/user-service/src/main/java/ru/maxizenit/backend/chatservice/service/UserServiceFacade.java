package ru.maxizenit.backend.chatservice.service;

import java.util.Collection;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxizenit.backend.chatservice.entity.Interest;
import ru.maxizenit.backend.chatservice.entity.UserProfile;

@Service
@RequiredArgsConstructor
public class UserServiceFacade {

  private final UserProfileService userProfileService;
  private final InterestService interestService;

  public UserProfile createUserProfile(
      String id, String firstName, String lastName, Date birthdate, Collection<Long> interestsIds) {
    Collection<Interest> interests = interestService.getInterestsByIds(interestsIds);
    return userProfileService.createUserProfile(id, firstName, lastName, birthdate, interests);
  }

  public UserProfile updateUserProfile(
      String id, String firstName, String lastName, Date birthdate, Collection<Long> interestIds) {
    Collection<Interest> interests = interestService.getInterestsByIds(interestIds);
    return userProfileService.updateUserProfile(id, firstName, lastName, birthdate, interests);
  }
}
