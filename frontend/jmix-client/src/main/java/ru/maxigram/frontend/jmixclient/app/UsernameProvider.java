package ru.maxigram.frontend.jmixclient.app;

import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsernameProvider {

  @Autowired private CurrentAuthentication currentAuthentication;

  public String getUsername() {
    return currentAuthentication.getUser().getUsername();
  }
}
