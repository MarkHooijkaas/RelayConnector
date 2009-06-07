package org.kisst.cfg4j;

import java.util.HashMap;


public class LayeredProps implements Props {
	private static final long serialVersionUID = 1L;

	private final HashMap<String, Object> map=new HashMap<String, Object>();
	private final Props parent;
	public LayeredProps(Props parent) {
		this.parent=parent;
	}

	public void put(String key, String value) {
		map.put(key,value);
	}

	public Object get(String key, Object defaultValue) {
		Object result=map.get(key);
		if (result!=null)
			return result;
		else if (parent != null)
			return parent.get(key, defaultValue);
		else
			return defaultValue;
	}

	public String getString(String key) {
		return (String) get(key);
	}

	public int getInt(String key) {
		return Integer.parseInt(getString(key));
	}

	public long getLong(String key) {
		return Long.parseLong(getString(key));
	}

	public Object get(String key) {
		Object result=map.get(key);
		if (result!=null)
			return result;
		else if (parent != null)
			return parent.get(key);
		else
			throw new RuntimeException("Could not find property "+key);
	}

	public String getString(String key, String defaultValue) {
		return (String) get(key,defaultValue);
	}

	public int getInt(String key, int defaultValue) {
		String s=getString(key,null);
		if (s==null)
			return defaultValue;
		else
			return Integer.parseInt(s);
	}

	public long getLong(String key, long defaultValue) {
		String s=getString(key,null);
		if (s==null)
			return defaultValue;
		else
			return Long.parseLong(s);
	}

	public boolean getBoolean(String name, boolean defaultValue) {
		String value=getString(name, null);
		if (value==null)
			 return defaultValue;
		else 
			return getBoolean(name);
	}

	public boolean getBoolean(String name) {
		String value=getString(name, null);
		if (value==null)
			 throw new RuntimeException("property "+name+" is not optional");
		if ("true".equals(value))
			return true;
		else if ("false".equals(value))
			return false;
		else
			throw new RuntimeException("property "+name+" should be true or false, not "+value);
	}

}
