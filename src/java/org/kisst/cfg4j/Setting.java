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

  public void set(Properties props, String parentAlias) {
	String path=parentAlias+name;
	String str=(String) props.get(path);
	if (str!=null && str.startsWith("@")) {
		path=str.substring(1).trim();
		str=(String) props.get(path);
	}
	if (str!=null) {
		//logger.debug("Setting "+fullName+" to "+str);
		set(str);
	}
  }


  public void set(Properties props) { 
	  if (parent==null)
		  set(props, ""); 
	  else
		  set(props, parent.fullName+"."); 
  }


  abstract public void set(String value);
  abstract public String asString();
}
