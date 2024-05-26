package ru.maxizenit.backend.apiservicehttpclient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import ru.maxigram.backend.maxigramcommons.dto.Comment;
import ru.maxigram.backend.maxigramcommons.dto.Post;
import ru.maxigram.backend.maxigramcommons.dto.request.CreatePostRequest;

public class PostsControllerHttpClient extends AbstractControllerHttpClient {

  private static final String CONTROLLER_PATH = "/posts";

  public PostsControllerHttpClient(RestTemplate restTemplate, String serviceUri) {
    super(restTemplate, serviceUri, CONTROLLER_PATH);
  }

  public Collection<Post> getFeed(String requesterId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("requesterId", requesterId);
    return get("", new ParameterizedTypeReference<>() {}, queryParams);
  }

  public Post createPost(String authorId, String text) {
    CreatePostRequest request = CreatePostRequest.builder().authorId(authorId).text(text).build();
    return post("", Post.class, null, request);
  }

  public Post getPostById(Long id, String requesterId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("requesterId", requesterId);
    return get(String.format("/%d", id), Post.class, queryParams);
  }

  public Post likePost(Long id, String requesterId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("requesterId", requesterId);
    return post(String.format("/%d/like", id), Post.class, queryParams, null);
  }

  public Post unlikePost(Long id, String requesterId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("requesterId", requesterId);
    return post(String.format("/%d/like", id), Post.class, queryParams, null);
  }

  public Collection<Comment> getCommentsForPost(Long id, String requesterId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("requesterId", requesterId);
    return get(
        String.format("/%d/comments", id), new ParameterizedTypeReference<>() {}, queryParams);
  }
}
