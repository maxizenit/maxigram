package ru.maxigram.frontend.jmixclient.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.maxizenit.backend.apiservicehttpclient.*;

@Configuration
public class ApiServiceHttpClientConfiguration {

  @Value("${api-service.url}")
  String apiServiceUrl;

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public ChatsControllerHttpClient chatsControllerHttpClient() {
    return new ChatsControllerHttpClient(restTemplate(), apiServiceUrl);
  }

  @Bean
  public CommentsControllerHttpClient commentsControllerHttpClient() {
    return new CommentsControllerHttpClient(restTemplate(), apiServiceUrl);
  }

  @Bean
  public InterestsControllerHttpClient interestsControllerHttpClient() {
    return new InterestsControllerHttpClient(restTemplate(), apiServiceUrl);
  }

  @Bean
  public PostsControllerHttpClient postsControllerHttpClient() {
    return new PostsControllerHttpClient(restTemplate(), apiServiceUrl);
  }

  @Bean
  public UsersControllerHttpClient usersControllerHttpClient() {
    return new UsersControllerHttpClient(restTemplate(), apiServiceUrl);
  }
}
