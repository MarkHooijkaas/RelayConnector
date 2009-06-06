package org.kisst.cfg4j;


public class StringSetting extends Setting {
  private final String value;

  public StringSetting(CompositeSetting parent, String name, String defaultValue) {
	super(parent,name);
	this.value=parent.getProperties().getProperty(fullName, defaultValue);
  }
  public String get() { return this.value; }
  public String asString() { return value; }
}
