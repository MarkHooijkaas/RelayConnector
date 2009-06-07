package org.kisst.cfg4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class MultiLevelSettings<T extends Setting> {
	private final HashMap<String,T> items=new HashMap<String,T>();
	private final Constructor<T> constructor;
	private final String name;
	private final MultiLevelProps mlprops;
	private final T globalSettings;
	
	public MultiLevelSettings(MultiLevelProps mlprops, String name, Class<T> clazz) {
		this.name=name;
		this.mlprops=mlprops;
		try {
			constructor=clazz.getConstructor(new Class[] {Props.class, String.class});
		} catch (NoSuchMethodException e) { throw new RuntimeException(e); }
		globalSettings = construct(name, mlprops.getGlobalProps());
	}

	public T getGlobalSettings() { return globalSettings; }
	public T get(String key) {
		T result=items.get(key);
		if (result!=null)
			return result;
		Props p=mlprops.getProps(key);
		if (p==null)
			return globalSettings;
		result = construct(name, p);
		items.put(key, result);
		return result;
	}

	private T construct(String name, Props p) {
		try {
			return constructor.newInstance(new Object[] {p, name});
		}
		catch (IllegalArgumentException e) { throw new RuntimeException(e); }
		catch (InstantiationException e) { throw new RuntimeException(e); }
		catch (IllegalAccessException e) { throw new RuntimeException(e); }
		catch (InvocationTargetException e) { throw new RuntimeException(e); }
	}
}
