package ru.maxizenit.backend.apiservice.controller;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.maxigram.backend.apicommons.dto.ChatListElement;
import ru.maxigram.backend.apicommons.dto.Message;
import ru.maxizenit.backend.apiservice.service.ChatService;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @GetMapping("/{id}/messages")
  public ResponseEntity<Collection<Message>> getMessagesFromChat(
      @PathVariable Long id, @RequestParam String userId) {
    return new ResponseEntity<>(chatService.getMessagesFromChat(id, userId), HttpStatus.OK);
  }

  @GetMapping
  public ResponseEntity<Collection<ChatListElement>> getChatsByUserId(@RequestParam String userId) {
    return new ResponseEntity<>(chatService.getChatsByUserId(userId), HttpStatus.OK);
  }
}
