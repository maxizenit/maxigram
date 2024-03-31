package ru.maxigram.backend.userservice.repository;

import org.springframework.data.repository.CrudRepository;
import ru.maxigram.backend.userservice.entity.UserProfile;

public interface UserProfileRepository extends CrudRepository<UserProfile, String> {}
