package ru.maxigram.frontend.jmixclient.view.profile;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import ru.maxigram.backend.maxigramcommons.dto.Interest;
import ru.maxigram.backend.maxigramcommons.dto.User;
import ru.maxigram.frontend.jmixclient.util.UserIdProvider;
import ru.maxigram.frontend.jmixclient.view.main.MainView;
import ru.maxizenit.backend.apiservicehttpclient.InterestsControllerHttpClient;
import ru.maxizenit.backend.apiservicehttpclient.UsersControllerHttpClient;

@Route(value = "profile", layout = MainView.class)
@ViewController("ProfileView")
@ViewDescriptor("profile-view.xml")
public class ProfileView extends StandardView {
  @ViewComponent private TypedDatePicker<Comparable> birthDateField;
  @ViewComponent private TypedTextField<String> firstNameField;
  @ViewComponent private JmixMultiSelectComboBox<Interest> interestsCombo;
  @ViewComponent private TypedTextField<String> lastNameField;
  @ViewComponent private JmixButton saveButton;

  @Autowired UsersControllerHttpClient usersControllerHttpClient;
  @Autowired private InterestsControllerHttpClient interestsControllerHttpClient;
  @Autowired private UserIdProvider userIdProvider;

  @Install(to = "interestsCombo", subject = "itemLabelGenerator")
  private String interestsComboItemLabelGenerator(final Interest t) {
    return t.getName();
  }

  @Subscribe
  public void onInit(final InitEvent event) {
    interestsCombo.setItems(interestsControllerHttpClient.getAllInterests());

    User user = usersControllerHttpClient.getUserById(userIdProvider.getUserId());

    if (user != null) {
      firstNameField.setValue(user.getFirstName());
      lastNameField.setValue(user.getLastName());
      birthDateField.setValue(
          user.getBirthdate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
      interestsCombo.select(user.getInterests());
    }
  }

  @Subscribe(id = "saveButton", subject = "clickListener")
  public void onSaveButtonClick(final ClickEvent<JmixButton> event) {
    User user = usersControllerHttpClient.getUserById(userIdProvider.getUserId());
    if (user != null) {
      usersControllerHttpClient.updateUser(
          userIdProvider.getUserId(),
          firstNameField.getValue(),
          lastNameField.getValue(),
          Date.from(birthDateField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
          interestsCombo.getValue().stream().map(Interest::getId).toList());
    } else {
      usersControllerHttpClient.createUser(
          userIdProvider.getUserId(),
          firstNameField.getValue(),
          lastNameField.getValue(),
          Date.from(birthDateField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
          interestsCombo.getSelectedItems().stream().map(Interest::getId).toList());
    }
  }
}
