package ru.maxigram.backend.userservice.service.impl;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxigram.backend.userservice.entity.Interest;
import ru.maxigram.backend.userservice.entity.UserProfile;
import ru.maxigram.backend.userservice.repository.UserProfileRepository;
import ru.maxigram.backend.userservice.service.UserProfileService;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

  private final UserProfileRepository userProfileRepository;

  @Override
  public Optional<UserProfile> findUserProfileById(String id) {
    return userProfileRepository.findById(id);
  }

  @Override
  public UserProfile createUserProfile(
      String userId, Date birthdate, String firstName, String lastName) {
    Optional<UserProfile> existingProfile = userProfileRepository.findById(userId);
    if (existingProfile.isPresent()) {
      throw new RuntimeException("User profile already exists");
    }

    UserProfile userProfile = new UserProfile();
    userProfile.setUserId(userId);
    userProfile.setBirthdate(birthdate);
    userProfile.setFirstName(firstName);
    userProfile.setLastName(lastName);

    return userProfileRepository.save(userProfile);
  }

  @Override
  public void addInterestToUserProfile(UserProfile userProfile, Interest interest) {
    userProfile.getInterests().add(interest);
    userProfileRepository.save(userProfile);
  }

  @Override
  public Collection<UserProfile> getAllUserProfiles() {
    return (Collection<UserProfile>) userProfileRepository.findAll();
  }
}
