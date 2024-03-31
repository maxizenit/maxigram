package ru.maxigram.frontend.jmixclient.view.chat;

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
import io.jmix.flowui.view.*;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import ru.maxigram.backend.apicommons.dto.Message;
import ru.maxigram.frontend.jmixclient.app.ApiServiceRestTemplateDecorator;
import ru.maxigram.frontend.jmixclient.app.UsernameProvider;
import ru.maxigram.frontend.jmixclient.view.main.MainView;

@Route(value = "messages", layout = MainView.class)
@ViewController("MessagesView")
@ViewDescriptor("messages-view.xml")
public class MessagesView extends StandardView {

  @ViewComponent private JmixListBox<Message> messageList;

  @Autowired private CurrentChatIdProvider currentChatIdProvider;

  @Autowired private UsernameProvider usernameProvider;

  @Autowired private ApiServiceRestTemplateDecorator apiServiceRestTemplateDecorator;

  @Autowired private UiComponents uiComponents;

  @Supply(to = "messageList", subject = "renderer")
  private ComponentRenderer<HorizontalLayout, Message> messageListRenderer() {
    return new ComponentRenderer<>(
        message -> {
          HorizontalLayout row = uiComponents.create(HorizontalLayout.class);
          row.setAlignItems(FlexComponent.Alignment.CENTER);

          Span text = new Span(message.getText());

          String ownInfo =
              message.getSenderId().equals(usernameProvider.getUsername()) ? "Вы" : "Собеседник";
          String timestampFormatted = new SimpleDateFormat("HH:mm").format(message.getTimestamp());
          String readInfo = message.getRead() ? "прочитано" : "не прочитано";

          Span info = new Span(String.format("%s, %s, %s", ownInfo, timestampFormatted, readInfo));
          info.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XXSMALL);

          VerticalLayout column = uiComponents.create(VerticalLayout.class);
          column.add(text, info);
          column.setPadding(false);
          column.setSpacing(false);

          row.add(column);
          row.addClassName(LumoUtility.LineHeight.MEDIUM);
          return row;
        });
  }

  // todo: удалить
  @ViewComponent private JmixTextArea messageTextArea;

  @Subscribe
  public void onInit(final InitEvent event) {
    fillMessageList();
  }

  private void fillMessageList() {
    Map<String, String> uriVariables = new HashMap<>();
    uriVariables.put("userId", usernameProvider.getUsername());

    Collection<Message> messages =
        apiServiceRestTemplateDecorator.get(
            "/chats/" + currentChatIdProvider.getCurrentChatId() + "/messages",
            new ParameterizedTypeReference<>() {},
            uriVariables);
    messageList.setItems(messages);
  }
}
