package ru.maxigram.frontend.jmixclient.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class SelectedUseridProvider {

  private String selectedUserid;
}
