package org.kisst.cfg4j;



public class BooleanSetting extends Setting {
  private final boolean value;

  public BooleanSetting(CompositeSetting parent, String name, boolean defaultValue) {
	super(parent, name);
	String value=parent.getProperties().getProperty(fullName, null);
	if (value==null)
		this.value=defaultValue;
	else if ("true".equals(value))
		this.value=true;
	else if ("false".equals(value))
		this.value=false;
	else
		throw new RuntimeException("property "+fullName+" should be true or false, not "+value);
  }
  public boolean get() { return this.value; }
  public String asString() { return Boolean.toString(value); }

}
