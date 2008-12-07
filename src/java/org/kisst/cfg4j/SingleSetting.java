package org.kisst.cfg4j;


public abstract class SingleSetting extends Setting {
  public SingleSetting(MultiSetting parent, String name) {
	  super(parent, name);
  }

  abstract public String asString();
}
