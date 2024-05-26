package ru.maxizenit.backend.apiservicehttpclient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import ru.maxigram.backend.maxigramcommons.dto.Chat;
import ru.maxigram.backend.maxigramcommons.dto.Message;
import ru.maxigram.backend.maxigramcommons.dto.request.SendMessageRequest;

public class ChatsControllerHttpClient extends AbstractControllerHttpClient {

  private static final String CONTROLLER_PATH = "/chats";

  public ChatsControllerHttpClient(RestTemplate restTemplate, String serviceUri) {
    super(restTemplate, serviceUri, CONTROLLER_PATH);
  }

  public Collection<Chat> getAllChatsByUserId(String userId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("userId", userId);
    return get("", new ParameterizedTypeReference<>() {}, queryParams);
  }

  public Chat getChatById(Long chatId, String requesterId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("chatId", chatId.toString());
    return get(String.format("/%d", chatId), Chat.class, queryParams);
  }

  public Collection<Message> getMessagesByChatId(Long chatId, String requesterId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("requesterId", requesterId);
    return get(
        String.format("/%d/messages", chatId), new ParameterizedTypeReference<>() {}, queryParams);
  }

  public Message sendMessageInChat(Long chatId, String senderId, String text) {
    SendMessageRequest request = SendMessageRequest.builder().senderId(senderId).text(text).build();
    return post(String.format("/%d/messages", chatId), Message.class, null, request);
  }

  public Chat closeAnonymousChat(Long chatId, String requesterId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("requesterId", requesterId);
    return post(String.format("/%d/close", chatId), Chat.class, queryParams, null);
  }

  public Chat agreeToDeAnonymization(Long chatId, String requesterId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("requesterId", requesterId);
    return post(
        String.format("/%d/agree-to-de-anonymization", chatId), Chat.class, queryParams, null);
  }

  public void sendRequestForAnonymousChat(String requesterId) {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("requesterId", requesterId);
    post("/request-for-anonymous-chat", Object.class, queryParams, null);
  }
}
