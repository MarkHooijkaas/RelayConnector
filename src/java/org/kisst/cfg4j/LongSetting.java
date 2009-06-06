package org.kisst.cfg4j;


public class LongSetting extends Setting {
  private final long value;

  public LongSetting(CompositeSetting parent, String name, long defaultValue) {
	super(parent, name);
	String value=parent.getProperties().getProperty(fullName, null);
	if (value==null)
		this.value=defaultValue;
	else
		this.value=Long.parseLong(value);
  }
  public String asString() { return Long.toString(value); }
  public long get() { return this.value; }
}
