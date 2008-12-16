package org.kisst.cfg4j;

import java.util.Properties;

public abstract class Setting {
  public final MultiSetting parent;
  public final String fullName;
  public final String name;
  public boolean required;
  protected boolean isSet;

  public Setting(MultiSetting parent, String name) {
	this.parent=parent;
	this.name=name;
	this.required=true;
	this.isSet=false;
	if(parent==null) 
	  fullName=name;
	else {
		if (parent.fullName==null)
			fullName=name;
		else
			fullName=parent.fullName+"."+name;
	  parent.add(this);
	}
  }
  
  public boolean ok() { return this.isSet || ! this.required; }



  public void set(Properties props) {
	  String value=props.getProperty(fullName);
	  if (value!=null)
		  set(value);
  }


  abstract public void set(String value);
  abstract public String asString();
}
