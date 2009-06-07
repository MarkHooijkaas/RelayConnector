package org.kisst.cfg4j;

public interface Props {
	public Object get(String key, Object defaultValue);
	public String getString(String key, String defaultValue);
	public int    getInt(String string, int defaultValue);
	public long   getLong(String string, long defaultValue);
	public boolean getBoolean(String name, boolean defaultValue);

	public Object get(String key);
	public String getString(String key);
	public int    getInt(String string);
	public long   getLong(String string);
	public boolean getBoolean(String name);
}
