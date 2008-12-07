package org.kisst.cfg4j;


public class BooleanSetting extends SingleSetting {
  private boolean value;

  public BooleanSetting(MultiSetting parent, String name) {
	super(parent,name);
  }

  public BooleanSetting(MultiSetting parent, String name, boolean defaultValue) {
	this(parent, name);
	set(defaultValue);
  }

  public void set(String str) {
	if (str==null) {
	  set(false); // TODO: set false, or ignore
	  return; 
	}
	str=str.toUpperCase();
	boolean result=false;
	if (str.equals("TRUE")) result=true;
	if (str.equals("ON")) result=true;
	if (str.equals("1")) result=true;
	set(result);
  }

  public boolean get() { return this.value; }
  public void set(boolean value) { 
	//MultiSetting.logger.debug("Setting "+fullName+" to "+value);
	this.value=value; 
    this.isSet=true;
  }

  public String asString() { return Boolean.toString(value); }

}
