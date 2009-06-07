package org.kisst.cfg4j;

public class IntSetting extends Setting {
	private final int defaultValue;

	public IntSetting(CompositeSetting parent, String name, int defaultValue) {
		super(parent, name);
		this.defaultValue=defaultValue;
	}
	public int get(Props props) {	return props.getInt(fullName, defaultValue);  }
}
