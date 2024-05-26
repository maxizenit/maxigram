package ru.maxizenit.backend.apiservicehttpclient;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import ru.maxigram.backend.maxigramcommons.dto.Message;
import ru.maxigram.backend.maxigramcommons.dto.SelfRestraint;
import ru.maxigram.backend.maxigramcommons.dto.Subscription;
import ru.maxigram.backend.maxigramcommons.dto.User;
import ru.maxigram.backend.maxigramcommons.dto.request.CreateOrUpdateUserRequest;
import ru.maxigram.backend.maxigramcommons.dto.request.SendMessageRequest;

public class UsersControllerHttpClient extends AbstractControllerHttpClient {

  private static final String CONTROLLER_PATH = "/users";

  public UsersControllerHttpClient(RestTemplate restTemplate, String serviceUri) {
    super(restTemplate, serviceUri, CONTROLLER_PATH);
  }

  public User getUserById(String id) {
    return get(String.format("/%s", id), User.class, null);
  }

  public User createUser(
      String id, String firstName, String lastName, Date birthdate, Collection<Long> interestsIds) {
    CreateOrUpdateUserRequest request =
        CreateOrUpdateUserRequest.builder()
            .id(id)
            .firstName(firstName)
            .lastName(lastName)
            .birthdate(birthdate)
            .interestsIds(interestsIds)
            .build();
    return post("", User.class, null, request);
  }

  public User updateUser(
      String id, String firstName, String lastName, Date birthdate, Collection<Long> interestsIds) {
    CreateOrUpdateUserRequest request =
        CreateOrUpdateUserRequest.builder()
            .id(id)
            .firstName(firstName)
            .lastName(lastName)
            .birthdate(birthdate)
            .interestsIds(interestsIds)
            .build();
    return put("", User.class, null, request);
  }

  public Collection<User> getAllUsers() {
    return get("", new ParameterizedTypeReference<>() {}, null);
  }

  public Collection<Subscription> getSubscriptionsByUserId(String id) {
    return get(String.format("/%s/subscriptions", id), new ParameterizedTypeReference<>() {}, null);
  }

  public Subscription subscribeOnUserById(String id, String subscriberId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("subscriberId", subscriberId);
    return post(String.format("/%s/subscribe", id), Subscription.class, queryParams, null);
  }

  public SelfRestraint getSelfRestraint(String id) {
    return get(String.format("/%s/self-restraint", id), SelfRestraint.class, null);
  }

  public SelfRestraint addSelfRestraint(String userId, Timestamp startTime, Timestamp endTime) {
    SelfRestraint request =
        SelfRestraint.builder().userId(userId).startTime(startTime).endTime(endTime).build();
    return post("/self-restraint", SelfRestraint.class, null, request);
  }

  public Message sendMessage(String id, String senderId, String text) {
    SendMessageRequest request = SendMessageRequest.builder().senderId(senderId).text(text).build();
    return post(String.format("/%s/send-message", id), Message.class, null, request);
  }
}
