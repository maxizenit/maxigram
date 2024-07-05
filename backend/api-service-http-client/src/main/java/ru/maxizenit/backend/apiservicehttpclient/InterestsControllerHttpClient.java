package ru.maxizenit.backend.apiservicehttpclient;

import java.util.Collection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import ru.maxigram.backend.maxigramcommons.dto.Interest;

public class InterestsControllerHttpClient extends AbstractControllerHttpClient {

  private static final String CONTROLLER_PATH = "/interests";

  public InterestsControllerHttpClient(RestTemplate restTemplate, String serviceUri) {
    super(restTemplate, serviceUri, CONTROLLER_PATH);
  }

  public Collection<Interest> getAllInterests() {
    return get("", new ParameterizedTypeReference<>() {}, null);
  }
}
