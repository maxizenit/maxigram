package ru.maxizenit.backend.apiservicehttpclient;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class AbstractControllerHttpClient {

  private final RestTemplate restTemplate;
  private final String serviceUri;
  private final String controllerPath;

  public AbstractControllerHttpClient(
      RestTemplate restTemplate, String serviceUri, String controllerPath) {
    this.restTemplate = restTemplate;
    this.serviceUri = serviceUri;
    this.controllerPath = controllerPath;
  }

  private String createUri(String path, Collection<String> paramKeys) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromHttpUrl(String.format("%s%s%s", serviceUri, controllerPath, path));

    for (String paramKey : paramKeys) {
      builder.queryParam(paramKey, String.format("{%s}", paramKey));
    }

    return builder.encode().toUriString();
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
    HttpEntity<?> requestEntity = new HttpEntity<>(requestBody);

    return restTemplate
        .exchange(
            uri,
            method,
            requestEntity,
            responseType,
            uriVariables != null ? uriVariables : Collections.emptyMap())
        .getBody();
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
    HttpEntity<?> requestEntity = new HttpEntity<>(requestBody);

    return restTemplate
        .exchange(
            uri,
            method,
            requestEntity,
            responseType,
            uriVariables != null ? uriVariables : Collections.emptyMap())
        .getBody();
  }

  protected final <T> T get(String path, Class<T> responseType, Map<String, ?> uriVariables) {
    return exchange(path, HttpMethod.GET, null, responseType, uriVariables);
  }

  protected final <T> T get(
      String path, ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) {
    return exchange(path, HttpMethod.GET, null, responseType, uriVariables);
  }

  protected final <T> T post(
      String path, Class<T> responseType, Map<String, ?> uriVariables, Object requestBody) {
    return exchange(path, HttpMethod.POST, requestBody, responseType, uriVariables);
  }

  protected final <T> T post(
      String path,
      ParameterizedTypeReference<T> responseType,
      Map<String, ?> uriVariables,
      Object requestBody) {
    return exchange(path, HttpMethod.POST, requestBody, responseType, uriVariables);
  }

  protected final <T> T put(
      String path, Class<T> responseType, Map<String, ?> uriVariables, Object requestBody) {
    return exchange(path, HttpMethod.PUT, requestBody, responseType, uriVariables);
  }

  protected final <T> T put(
      String path,
      ParameterizedTypeReference<T> responseType,
      Map<String, ?> uriVariables,
      Object requestBody) {
    return exchange(path, HttpMethod.PUT, requestBody, responseType, uriVariables);
  }
}
