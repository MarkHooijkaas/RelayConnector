package org.kisst.cfg4j;

import java.util.Properties;

public class IntSetting extends SingleSetting {
  private int value;

  public IntSetting(MultiSetting parent, String name) {
	super(parent,name);
  }

  public IntSetting(MultiSetting parent, String name, int defaultValue) {
	this(parent, name); 
	// TODO: remember default value?
	set(defaultValue);
  }

  public void set(String value) { set(Integer.parseInt( value)); }
  public String asString() { return Integer.toString(value); }

  public int get() { return this.value; }
  public void set(int value) { 
	MultiSetting.logger.debug("Setting "+fullName+" to "+value);
	this.value=value; 
	isSet=true;
  }

}
