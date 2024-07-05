package ru.maxigram.backend.maxigramcommons.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@EqualsAndHashCode
public class Interest {

  @EqualsAndHashCode.Include private Long id;
  private String name;
}
