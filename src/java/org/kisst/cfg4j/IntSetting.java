package org.kisst.cfg4j;

import java.util.Properties;

public class IntSetting extends Setting {
  private final int value;

  public IntSetting(Setting parent, String name, Properties props, int defaultValue) {
	super(parent, name);
	String value=props.getProperty(fullName, null);
	if (value==null)
		this.value=defaultValue;
	else
		this.value=Integer.parseInt(value);
  }
  public String asString() { return Integer.toString(value); }
  public int get() { return this.value; }
}
