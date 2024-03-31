package ru.maxigram.frontend.jmixclient.view.chat;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.listbox.JmixListBox;
import io.jmix.flowui.view.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import ru.maxigram.backend.apicommons.dto.ChatListElement;
import ru.maxigram.frontend.jmixclient.app.ApiServiceRestTemplateDecorator;
import ru.maxigram.frontend.jmixclient.app.UsernameProvider;
import ru.maxigram.frontend.jmixclient.view.main.MainView;

@Route(value = "chats", layout = MainView.class)
@ViewController("ChatsView")
@ViewDescriptor("chats-view.xml")
public class ChatsView extends StandardView {

  @ViewComponent private JmixListBox<ChatListElement> chatList;

  @Autowired private UiComponents uiComponents;

  @Autowired private ViewNavigators viewNavigators;

  @Autowired private CurrentChatIdProvider currentChatIdProvider;

  @Autowired private UsernameProvider usernameProvider;

  @Autowired private ApiServiceRestTemplateDecorator apiServiceRestTemplateDecorator;

  @Subscribe
  public void onInit(final InitEvent event) {
    fillChatList();
  }

  @Supply(to = "chatList", subject = "renderer")
  private ComponentRenderer<HorizontalLayout, ChatListElement> chatListRenderer() {
    return new ComponentRenderer<>(
        chat -> {
          HorizontalLayout row = uiComponents.create(HorizontalLayout.class);
          row.setAlignItems(FlexComponent.Alignment.CENTER);

          Span name =
              new Span(
                  String.format(
                      "%s %s",
                      chat.getOtherParticipantFirstName(), chat.getOtherParticipantLastName()));
          Span lastMessage = new Span(chat.getLastMessage());
          lastMessage.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

          VerticalLayout column = uiComponents.create(VerticalLayout.class);
          column.add(name, lastMessage);
          column.setPadding(false);
          column.setSpacing(false);

          row.add(column);
          row.addClassName(LumoUtility.LineHeight.MEDIUM);
          return row;
        });
  }

  @Subscribe("chatList")
  public void onChatListComponentValueChange(
      final AbstractField.ComponentValueChangeEvent<JmixListBox<ChatListElement>, ChatListElement>
          event) {
    currentChatIdProvider.setCurrentChatId(event.getValue().getChatId());
    viewNavigators.view(MessagesView.class).navigate();
  }

  private void fillChatList() {
    Map<String, String> uriVariables = new HashMap<>();
    uriVariables.put("userId", usernameProvider.getUsername());

    Collection<ChatListElement> chats =
        apiServiceRestTemplateDecorator.get(
            "/chats", new ParameterizedTypeReference<>() {}, uriVariables);
    chatList.setItems(chats);
  }
}
