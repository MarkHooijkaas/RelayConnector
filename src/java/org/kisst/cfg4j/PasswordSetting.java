package org.kisst.cfg4j;

import java.util.Properties;

public class PasswordSetting extends StringSetting {

  public PasswordSetting(ExpandableSetting parent, String name, Properties props, String defaultValue) {
	  super(parent, name, props, defaultValue);
  }

  public String asString() { return "[secret password]"; }
}
