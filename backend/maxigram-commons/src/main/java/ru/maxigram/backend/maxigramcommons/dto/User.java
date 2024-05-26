package ru.maxigram.backend.maxigramcommons.dto;

import java.util.Date;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class User {

  private String id;
  private String firstName;
  private String lastName;
  private Date birthdate;
  private List<Interest> interests;
}
