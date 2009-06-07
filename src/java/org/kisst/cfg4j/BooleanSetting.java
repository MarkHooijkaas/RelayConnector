package org.kisst.cfg4j;



public class BooleanSetting extends Setting {
	private final boolean defaultValue;

	public BooleanSetting(CompositeSetting parent, String name, boolean defaultValue) {
		super(parent, name);
		this.defaultValue=defaultValue;
	}
	public boolean get(Props props) {	
		return props.getBoolean(fullName, defaultValue);
	}
}
