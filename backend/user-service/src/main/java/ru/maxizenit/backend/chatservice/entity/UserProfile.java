package ru.maxizenit.backend.chatservice.entity;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class UserProfile {

  @Id private String id;
  private String firstName;
  private String lastName;
  private Date birthdate;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_interest",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "interest_id"))
  Collection<Interest> interests;

  @OneToOne(mappedBy = "userProfile")
  private SelfRestraint selfRestraint;

  @OneToMany(mappedBy = "subscriber")
  private Collection<Subscription> subscriptions;
}
