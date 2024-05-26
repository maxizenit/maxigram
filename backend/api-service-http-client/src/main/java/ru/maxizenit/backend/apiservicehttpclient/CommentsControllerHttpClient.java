package ru.maxizenit.backend.apiservicehttpclient;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.client.RestTemplate;
import ru.maxigram.backend.maxigramcommons.dto.Comment;
import ru.maxigram.backend.maxigramcommons.dto.request.AddCommentRequest;

public class CommentsControllerHttpClient extends AbstractControllerHttpClient {

  private static final String CONTROLLER_PATH = "/comments";

  public CommentsControllerHttpClient(RestTemplate restTemplate, String serviceUri) {
    super(restTemplate, serviceUri, CONTROLLER_PATH);
  }

  public Comment addComment(Long postId, String authorId, String text) {
    AddCommentRequest request =
        AddCommentRequest.builder().postId(postId).authorId(authorId).text(text).build();
    return post("", Comment.class, null, request);
  }

  public Comment likeComment(Long id, String requesterId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("requesterId", requesterId);
    return post(String.format("/%d/like", id), Comment.class, queryParams, null);
  }

  public Comment unlikeComment(Long id, String requesterId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("requesterId", requesterId);
    return post(String.format("/%d/unlike", id), Comment.class, queryParams, null);
  }
}
