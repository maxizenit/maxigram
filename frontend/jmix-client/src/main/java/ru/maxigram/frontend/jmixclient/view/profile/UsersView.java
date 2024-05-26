package ru.maxigram.frontend.jmixclient.view.profile;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.listbox.JmixListBox;
import io.jmix.flowui.view.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import ru.maxigram.backend.maxigramcommons.dto.User;
import ru.maxigram.frontend.jmixclient.util.SelectedUseridProvider;
import ru.maxigram.frontend.jmixclient.util.UserIdProvider;
import ru.maxigram.frontend.jmixclient.view.main.MainView;
import ru.maxizenit.backend.apiservicehttpclient.UsersControllerHttpClient;

@Route(value = "users-view", layout = MainView.class)
@ViewController("UsersView")
@ViewDescriptor("users-view.xml")
public class UsersView extends StandardView {

  @ViewComponent private JmixListBox<User> userList;

  @Autowired private UsersControllerHttpClient usersControllerHttpClient;
  @Autowired private UserIdProvider userIdProvider;
  @Autowired private SelectedUseridProvider selectedUseridProvider;
  @Autowired private UiComponents uiComponents;

  @Subscribe
  public void onInit(final InitEvent event) {
    fillUserList();
  }

  @Supply(to = "userList", subject = "renderer")
  private ComponentRenderer<HorizontalLayout, User> userListRenderer() {
    return new ComponentRenderer<>(
        user -> {
          HorizontalLayout row = uiComponents.create(HorizontalLayout.class);

          String name = String.format("%s %s", user.getFirstName(), user.getLastName());
          int age =
              Period.between(
                      user.getBirthdate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                      LocalDate.now())
                  .getYears();
          Span nameAndAge = new Span(String.format("%s, лет: %s", name, age));

          StringBuilder interestsBuilder = new StringBuilder();
          user.getInterests().forEach(i -> interestsBuilder.append(i.getName()).append(" "));

          Span interests = new Span(String.format("Интересы: %s", interestsBuilder));
          interests.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

          VerticalLayout column = uiComponents.create(VerticalLayout.class);
          column.add(nameAndAge, interests);
          column.setPadding(false);
          column.setSpacing(false);

          row.add(column);
          row.addClassName(LumoUtility.LineHeight.MEDIUM);
          return row;
        });
  }

  @Subscribe("userList")
  public void onUserListComponentValueChange(
      final AbstractField.ComponentValueChangeEvent<JmixListBox<User>, User> event) {
    selectedUseridProvider.setSelectedUserid(event.getValue().getId());
  }

  private void fillUserList() {
    Collection<User> users =
        usersControllerHttpClient.getAllUsers().stream()
            .filter(u -> !u.getId().equals(userIdProvider.getUserId()))
            .toList();
    userList.setItems(users);
  }
}
