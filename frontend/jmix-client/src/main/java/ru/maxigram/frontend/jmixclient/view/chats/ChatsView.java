package ru.maxigram.frontend.jmixclient.view.chats;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
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
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.maxigram.backend.maxigramcommons.dto.Chat;
import ru.maxigram.backend.maxigramcommons.dto.User;
import ru.maxigram.frontend.jmixclient.util.CurrentChatIdProvider;
import ru.maxigram.frontend.jmixclient.util.UserIdProvider;
import ru.maxigram.frontend.jmixclient.view.main.MainView;
import ru.maxizenit.backend.apiservicehttpclient.ChatsControllerHttpClient;
import ru.maxizenit.backend.apiservicehttpclient.UsersControllerHttpClient;

@Route(value = "chats", layout = MainView.class)
@ViewController("ChatsView")
@ViewDescriptor("chats-view.xml")
public class ChatsView extends StandardView {
  @ViewComponent private JmixListBox<Chat> chatList;
  @ViewComponent private JmixButton findAnonymousChatButton;

  @Autowired private ChatsControllerHttpClient chatsControllerHttpClient;
  @Autowired private UsersControllerHttpClient usersControllerHttpClient;
  @Autowired private UserIdProvider userIdProvider;
  @Autowired private CurrentChatIdProvider currentChatIdProvider;
  @Autowired private ViewNavigators viewNavigators;
  @Autowired private UiComponents uiComponents;

  @Subscribe(id = "findAnonymousChatButton", subject = "clickListener")
  public void onFindAnonymousChatButtonClick(final ClickEvent<JmixButton> event) {
    chatsControllerHttpClient.sendRequestForAnonymousChat(userIdProvider.getUserId());
  }

  @Subscribe("chatList")
  public void onChatListComponentValueChange(
      final AbstractField.ComponentValueChangeEvent<JmixListBox<Chat>, Chat> event) {
    currentChatIdProvider.setCurrentChatId(event.getValue().getId());
    viewNavigators.view(MessagesView.class).navigate();
  }

  @Subscribe
  public void onInit(final InitEvent event) {
    chatList.setItems(chatsControllerHttpClient.getAllChatsByUserId(userIdProvider.getUserId()));
  }

  @Supply(to = "chatList", subject = "renderer")
  private ComponentRenderer<HorizontalLayout, Chat> chatListRenderer() {
    return new ComponentRenderer<>(
        chat -> {
          HorizontalLayout row = uiComponents.create(HorizontalLayout.class);
          row.setAlignItems(FlexComponent.Alignment.CENTER);

          String otherParticipantId =
              userIdProvider.getUserId().equals(chat.getFirstParticipantId())
                  ? chat.getSecondParticipantId()
                  : chat.getFirstParticipantId();
          String otherParticipantName;
          if (chat.getAnonymous()) {
            otherParticipantName = "Анонимный собеседник";
          } else {
            User user = usersControllerHttpClient.getUserById(otherParticipantId);
            otherParticipantName = String.format("%s %s", user.getFirstName(), user.getLastName());
          }
          Span name = new Span(otherParticipantName);

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
}
