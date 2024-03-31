package ru.maxigram.frontend.jmixclient.view.chat;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class CurrentChatIdProvider {

  private Long currentChatId;
}
