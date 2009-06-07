package org.kisst.cfg4j;

public class LongSetting extends Setting {
	private final long defaultValue;

	public LongSetting(CompositeSetting parent, String name, long defaultValue) {
		super(parent, name);
		this.defaultValue=defaultValue;
	}
	public long get(Props props) {	return props.getLong(fullName, defaultValue);  }
}
