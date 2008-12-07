package org.kisst.cfg4j;

public class PasswordSetting extends StringSetting {
  public PasswordSetting(MultiSetting parent, String name) {
	super(parent,name);
  }

  public PasswordSetting(MultiSetting parent, String name, String defaultValue) {
	  super(parent, name, defaultValue);
  }

  public String asString() { return "[secret password]"; }
}
