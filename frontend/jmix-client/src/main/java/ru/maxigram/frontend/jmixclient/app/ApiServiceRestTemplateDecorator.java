package ru.maxigram.frontend.jmixclient.app;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ApiServiceRestTemplateDecorator {

  @Value("${api-service.url}")
  private String apiServiceUrl;

  @Autowired private RestTemplate restTemplate;

  private String createUri(String path, Collection<String> paramKeys) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromHttpUrl(String.format("%s%s", apiServiceUrl, path));

    for (String paramKey : paramKeys) {
      builder.queryParam(paramKey, String.format("{%s}", paramKey));
    }

    return builder.encode().toUriString();
  }

  private HttpEntity<?> createRequestEntity(Object body) {
    return new HttpEntity<>(body, null);
  }

  private <T> T exchange(
      String path,
      HttpMethod method,
      Object requestBody,
      Class<T> responseType,
      Map<String, ?> uriVariables) {
    String uri =
        createUri(
            path,
            CollectionUtils.isEmpty(uriVariables)
                ? Collections.emptyList()
                : uriVariables.keySet());
    HttpEntity<?> requestEntity = createRequestEntity(requestBody);

    return restTemplate.exchange(uri, method, requestEntity, responseType, uriVariables).getBody();
  }

  private <T> T exchange(
      String path,
      HttpMethod method,
      Object requestBody,
      ParameterizedTypeReference<T> responseType,
      Map<String, ?> uriVariables) {
    String uri =
        createUri(
            path,
            CollectionUtils.isEmpty(uriVariables)
                ? Collections.emptyList()
                : uriVariables.keySet());
    HttpEntity<?> requestEntity = createRequestEntity(requestBody);

    return restTemplate.exchange(uri, method, requestEntity, responseType, uriVariables).getBody();
  }

  public <T> T get(String path, Class<T> responseType, Map<String, ?> uriVariables) {
    return exchange(path, HttpMethod.GET, null, responseType, uriVariables);
  }

  public <T> T get(
      String path, ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) {
    return exchange(path, HttpMethod.GET, null, responseType, uriVariables);
  }
}
