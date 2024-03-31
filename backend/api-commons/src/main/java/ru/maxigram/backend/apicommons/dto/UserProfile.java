package ru.maxigram.backend.apicommons.dto;

import java.sql.Timestamp;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserProfile {

  private String id;

  private Timestamp birthdate;

  private String firstName;

  private String lastName;
}
