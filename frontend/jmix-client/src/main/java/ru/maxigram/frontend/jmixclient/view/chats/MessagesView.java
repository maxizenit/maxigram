package ru.maxigram.frontend.jmixclient.view.chats;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.listbox.JmixListBox;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import java.text.SimpleDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.maxigram.backend.maxigramcommons.dto.Chat;
import ru.maxigram.backend.maxigramcommons.dto.Message;
import ru.maxigram.frontend.jmixclient.util.CurrentChatIdProvider;
import ru.maxigram.frontend.jmixclient.util.UserIdProvider;
import ru.maxigram.frontend.jmixclient.view.main.MainView;
import ru.maxizenit.backend.apiservicehttpclient.ChatsControllerHttpClient;

@Route(value = "messages", layout = MainView.class)
@ViewController("MessagesView")
@ViewDescriptor("messages-view.xml")
public class MessagesView extends StandardView {

  @ViewComponent private JmixButton closeChatButton;
  @ViewComponent private JmixButton agreeToDeAnonymizationButton;
  @ViewComponent private JmixButton goInNewChatButton;
  @ViewComponent private JmixListBox<Message> messagesList;
  @ViewComponent private JmixTextArea messageTextArea;
  @ViewComponent private JmixButton sendMessageButton;

  @Autowired private ChatsControllerHttpClient chatsControllerHttpClient;
  @Autowired private CurrentChatIdProvider currentChatIdProvider;
  @Autowired private UserIdProvider userIdProvider;
  @Autowired private UiComponents uiComponents;

  @Subscribe(id = "sendMessageButton", subject = "clickListener")
  public void onSendMessageButtonClick(final ClickEvent<JmixButton> event) {
    String text = messageTextArea.getValue();
    if (!StringUtils.isEmpty(text)) {
      chatsControllerHttpClient.sendMessageInChat(
          currentChatIdProvider.getCurrentChatId(), userIdProvider.getUserId(), text);
      fillMessagesList();
      updateButtons();
    }
  }

  @Subscribe(id = "agreeToDeAnonymizationButton", subject = "clickListener")
  public void onAgreeToDeAnonymizationButtonClick(final ClickEvent<JmixButton> event) {
    chatsControllerHttpClient.agreeToDeAnonymization(
        currentChatIdProvider.getCurrentChatId(), userIdProvider.getUserId());
    updateButtons();
  }

  @Subscribe(id = "closeChatButton", subject = "clickListener")
  public void onCloseChatButtonClick(final ClickEvent<JmixButton> event) {
    chatsControllerHttpClient.closeAnonymousChat(
        currentChatIdProvider.getCurrentChatId(), userIdProvider.getUserId());
    updateButtons();
  }

  @Subscribe(id = "goInNewChatButton", subject = "clickListener")
  public void onGoInNewChatButtonClick(final ClickEvent<JmixButton> event) {
    Chat currentChat =
        chatsControllerHttpClient.getChatById(
            currentChatIdProvider.getCurrentChatId(), userIdProvider.getUserId());
    currentChatIdProvider.setCurrentChatId(currentChat.getNewChatId());

    onInit(new InitEvent(this));
  }

  @Subscribe
  public void onInit(final InitEvent event) {
    fillMessagesList();
  }

  private void updateButtons() {
    agreeToDeAnonymizationButton.setEnabled(true);
    closeChatButton.setEnabled(true);
    goInNewChatButton.setEnabled(true);
    sendMessageButton.setEnabled(true);

    Chat currentChat =
        chatsControllerHttpClient.getChatById(
            currentChatIdProvider.getCurrentChatId(), userIdProvider.getUserId());
    String currentUserId = userIdProvider.getUserId();

    if (currentChat.getAnonymous()) {
      if ((currentChat.getFirstParticipantId().equals(currentUserId)
              && currentChat.getFirstParticipantAgreeToDeAnonymization())
          || (currentChat.getSecondParticipantId().equals(currentUserId)
              && currentChat.getSecondParticipantAgreeToDeAnonymization())) {
        agreeToDeAnonymizationButton.setEnabled(false);
        agreeToDeAnonymizationButton.setTooltipText("Согласие уже подано");
      }
      if (currentChat.getIsClosed()) {
        closeChatButton.setEnabled(false);
        closeChatButton.setTooltipText("Чат уже закрыт");
      } else {
        sendMessageButton.setEnabled(false);
        sendMessageButton.setTooltipText("Чат закрыт");
      }
      if (currentChat.getNewChatId() == null) {
        goInNewChatButton.setEnabled(false);
        goInNewChatButton.setTooltipText(
            "Для создания обычного чата оба участника должны согласиться на деанонимизацию");
      }
    } else {
      agreeToDeAnonymizationButton.setVisible(false);
      closeChatButton.setVisible(false);
      goInNewChatButton.setVisible(false);
    }
  }

  private void fillMessagesList() {
    messagesList.setItems(
        chatsControllerHttpClient.getMessagesByChatId(
            currentChatIdProvider.getCurrentChatId(), userIdProvider.getUserId()));
  }

  @Supply(to = "messagesList", subject = "renderer")
  private ComponentRenderer<HorizontalLayout, Message> messagesListRenderer() {
    return new ComponentRenderer<>(
        message -> {
          HorizontalLayout row = uiComponents.create(HorizontalLayout.class);
          row.setAlignItems(FlexComponent.Alignment.CENTER);

          Span messageSpan = new Span(message.getText());

          Span info =
              new Span(
                  String.format(
                      "%s, %s, %s",
                      message.getSenderId().equals(userIdProvider.getUserId())
                          ? "Вы"
                          : "Собеседник",
                      new SimpleDateFormat("HH:mm").format(message.getTimestamp()),
                      message.getRead() ? "прочитано" : "не прочитано"));
          info.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

          VerticalLayout column = uiComponents.create(VerticalLayout.class);
          column.add(messageSpan, info);
          column.setPadding(false);
          column.setSpacing(false);

          row.add(column);
          row.addClassName(LumoUtility.LineHeight.MEDIUM);
          return row;
        });
  }
}
