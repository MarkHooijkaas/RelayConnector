package org.kisst.cfg4j;

public class StringSetting extends Setting {
	private final String defaultValue;

	public StringSetting(CompositeSetting parent, String name, String defaultValue) {
		super(parent, name);
		this.defaultValue=defaultValue;
	}
	public String get(Props props) {	return props.getString(fullName, defaultValue);  }
}
