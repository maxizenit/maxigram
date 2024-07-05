package ru.maxizenit.backend.chatservice.service;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.maxizenit.backend.chatservice.entity.Interest;
import ru.maxizenit.backend.chatservice.repository.InterestRepository;

@Service
@RequiredArgsConstructor
public class InterestService {

  private final InterestRepository interestRepository;

  public Collection<Interest> getAllInterests() {
    return (Collection<Interest>) interestRepository.findAll();
  }

  public Collection<Interest> getInterestsByIds(Collection<Long> ids) {
    return interestRepository.findAllWhereIdIn(ids);
  }
}
