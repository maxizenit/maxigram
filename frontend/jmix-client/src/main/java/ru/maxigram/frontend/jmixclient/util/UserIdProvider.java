package ru.maxigram.frontend.jmixclient.util;

import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserIdProvider {

  @Autowired private CurrentAuthentication currentAuthentication;

  public String getUserId() {
    return currentAuthentication.getUser().getUsername();
  }
}
