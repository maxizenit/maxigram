package ru.maxigram.backend.userservice.service;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import ru.maxigram.backend.userservice.entity.Interest;
import ru.maxigram.backend.userservice.entity.UserProfile;

public interface UserProfileService {

  Optional<UserProfile> findUserProfileById(String id);

  UserProfile createUserProfile(String userId, Date birthdate, String firstName, String lastName);

  void addInterestToUserProfile(UserProfile userProfile, Interest interest);

  Collection<UserProfile> getAllUserProfiles();
}
