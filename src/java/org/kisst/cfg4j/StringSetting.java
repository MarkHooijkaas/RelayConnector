package org.kisst.cfg4j;

import java.util.Properties;

public class StringSetting extends SingleSetting {
  private String value=null;

  public StringSetting(MultiSetting parent, String name) {
	super(parent,name);
  }

  public StringSetting(MultiSetting parent, String name, String defaultValue) {
	this(parent, name);
	// TODO: remember default value?
	set(defaultValue);
  }

  public String get() { return this.value; }
  public void set(String value) { 
	MultiSetting.logger.debug("Setting "+fullName+" to "+value);
	this.value=value;
	this.isSet=true;
  }

  public String asString() { return value; }
}
