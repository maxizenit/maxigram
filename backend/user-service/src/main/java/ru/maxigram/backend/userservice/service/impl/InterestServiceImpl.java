package ru.maxigram.backend.userservice.service.impl;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxigram.backend.userservice.entity.Interest;
import ru.maxigram.backend.userservice.repository.InterestRepository;
import ru.maxigram.backend.userservice.service.InterestService;

@Service
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {

  private final InterestRepository interestRepository;

  @Override
  public Optional<Interest> findInterestById(Long id) {
    return interestRepository.findById(id);
  }
}
