package org.kisst.cfg4j;


public class PasswordSetting extends StringSetting {

  public PasswordSetting(CompositeSetting parent, String name, String defaultValue) {
	  super(parent, name, defaultValue);
  }

  public String asString() { return "[secret password]"; }
}
