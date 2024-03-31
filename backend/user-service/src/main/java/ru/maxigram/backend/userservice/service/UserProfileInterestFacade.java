package ru.maxigram.backend.userservice.service;


public interface UserProfileInterestFacade {

  void addInterestToUserProfile(String userId, Long interestId);
}
