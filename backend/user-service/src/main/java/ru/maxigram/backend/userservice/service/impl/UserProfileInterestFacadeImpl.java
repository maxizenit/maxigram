package ru.maxigram.backend.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxigram.backend.userservice.entity.Interest;
import ru.maxigram.backend.userservice.entity.UserProfile;
import ru.maxigram.backend.userservice.exception.InterestNotFoundException;
import ru.maxigram.backend.userservice.exception.UserProfileNotFoundException;
import ru.maxigram.backend.userservice.service.InterestService;
import ru.maxigram.backend.userservice.service.UserProfileInterestFacade;
import ru.maxigram.backend.userservice.service.UserProfileService;

@Service
@RequiredArgsConstructor
public class UserProfileInterestFacadeImpl implements UserProfileInterestFacade {

  private final UserProfileService userProfileService;
  private final InterestService interestService;

  @Override
  public void addInterestToUserProfile(String userId, Long interestId) {
    UserProfile userProfile =
        userProfileService
            .findUserProfileById(userId)
            .orElseThrow(() -> new UserProfileNotFoundException(userId));
    Interest interest =
        interestService
            .findInterestById(interestId)
            .orElseThrow(() -> new InterestNotFoundException(interestId));

    userProfileService.addInterestToUserProfile(userProfile, interest);
  }
}
