package org.kisst.cfg4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;


public class MappedSetting<T extends Setting> extends CompositeSetting {
	// TODO: This is a cache, but it is reset-proof
	private final HashMap<String,T> items=new HashMap<String,T>();
	private final Constructor constructor;
	
	public MappedSetting(CompositeSetting parent, String name, Class<?> clazz) { 
		super(parent, name);
		try {
			constructor=clazz.getConstructor(new Class[] {CompositeSetting.class, String.class});
		} catch (NoSuchMethodException e) { throw new RuntimeException(e); }
	}


	@SuppressWarnings("unchecked")
	public T get(String name) {
		T result=items.get(name);
		if (result==null) {
			try {
				result= (T) constructor.newInstance(new Object[] {this, name});
			}
			catch (IllegalArgumentException e) { throw new RuntimeException(e); }
			catch (InstantiationException e) { throw new RuntimeException(e); }
			catch (IllegalAccessException e) { throw new RuntimeException(e); }
			catch (InvocationTargetException e) { throw new RuntimeException(e); }
			items.put(name, result);
		}
		return  result;
	}
}
