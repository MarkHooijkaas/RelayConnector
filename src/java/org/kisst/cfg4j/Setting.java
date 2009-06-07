package org.kisst.cfg4j;


public class Setting {
  protected final CompositeSetting parent;
  protected final String fullName;
  protected final String name;
  //private final boolean required;

  public Setting(CompositeSetting parent, String name) {
	this.parent=parent;
	this.name=name;
	//this.required=required;
	if(parent==null) 
	  fullName=name;
	else {
		parent.add(this);
		if (parent.fullName==null)
			fullName=name;
		else
			fullName=parent.fullName+"."+name;
	}
  }
  
  //public boolean ok() { return ! this.required; }


  //abstract public String asString();
}
