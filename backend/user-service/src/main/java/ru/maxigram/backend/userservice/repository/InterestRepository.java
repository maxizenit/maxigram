package ru.maxigram.backend.userservice.repository;

import org.springframework.data.repository.CrudRepository;
import ru.maxigram.backend.userservice.entity.Interest;

public interface InterestRepository extends CrudRepository<Interest, Long> {}
