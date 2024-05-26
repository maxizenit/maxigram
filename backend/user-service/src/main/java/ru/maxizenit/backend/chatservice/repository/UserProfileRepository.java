package ru.maxizenit.backend.chatservice.repository;

import org.springframework.data.repository.CrudRepository;
import ru.maxizenit.backend.chatservice.entity.UserProfile;

public interface UserProfileRepository extends CrudRepository<UserProfile, String> {}
