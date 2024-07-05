package ru.maxigram.frontend.jmixclient.security;

import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "UiMinimal", code = UiMinimalRole.CODE)
public interface UiMinimalRole {

  String CODE = "ui-minimal";

  @ViewPolicy(viewIds = "*")
  @MenuPolicy(menuIds = "*")
  @SpecificPolicy(resources = "*")
  void uiMinimal();
}
