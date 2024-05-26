package ru.maxigram.backend.maxigramcommons.dto.request;

import java.util.Collection;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class CreateOrUpdateUserRequest {

  private String id;
  private String firstName;
  private String lastName;
  private Date birthdate;
  private Collection<Long> interestsIds;
}
