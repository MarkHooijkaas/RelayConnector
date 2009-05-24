package org.kisst.cfg4j;

import java.util.Properties;

public class LongSetting extends Setting {
  private final long value;

  public LongSetting(Setting parent, String name, Properties props, long defaultValue) {
	super(parent, name);
	String value=props.getProperty(fullName, null);
	if (value==null)
		this.value=defaultValue;
	else
		this.value=Long.parseLong(value);
  }
  public String asString() { return Long.toString(value); }
  public long get() { return this.value; }
}
