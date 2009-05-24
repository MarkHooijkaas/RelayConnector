package org.kisst.cfg4j;

import java.util.Properties;

public class StringSetting extends Setting {
  private final String value;

  public StringSetting(Setting parent, String name, Properties props, String defaultValue) {
	super(parent,name);
	this.value=props.getProperty(fullName, defaultValue);
  }
  public String get() { return this.value; }
  public String asString() { return value; }
}
