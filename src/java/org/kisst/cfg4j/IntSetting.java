package org.kisst.cfg4j;


public class IntSetting extends Setting {
  private final int value;

  public IntSetting(CompositeSetting parent, String name, int defaultValue) {
	super(parent, name);
	String value=parent.getProperties().getProperty(fullName, null);
	if (value==null)
		this.value=defaultValue;
	else
		this.value=Integer.parseInt(value);
  }
  public String asString() { return Integer.toString(value); }
  public int get() { return this.value; }
}
