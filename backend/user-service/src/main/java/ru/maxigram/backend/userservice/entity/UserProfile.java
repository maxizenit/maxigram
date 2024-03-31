package ru.maxigram.backend.userservice.entity;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserProfile {

  @Id private String userId;

  private Date birthdate;

  private String firstName;

  private String lastName;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_profile_interest",
      joinColumns = {@JoinColumn(name = "user_profile_id")},
      inverseJoinColumns = {@JoinColumn(name = "interest_id")})
  private Collection<Interest> interests;
}
