package ru.maxigram.backend.userservice.service;

import java.util.Optional;
import ru.maxigram.backend.userservice.entity.Interest;

public interface InterestService {

  Optional<Interest> findInterestById(Long id);
}
