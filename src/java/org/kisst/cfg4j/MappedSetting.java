package org.kisst.cfg4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Properties;

import org.kisst.cfg4j.MultiSetting;


public class MappedSetting<T extends Setting> extends MultiSetting {
	private Properties properties;
	private final HashMap<String,T> items=new HashMap<String,T>();
	private final Constructor constructor;
	
	public MappedSetting(MultiSetting parent, String name, Class<?> clazz) { 
		super(parent, name);
		try {
			constructor=clazz.getConstructor(new Class[] {MultiSetting.class, String.class});
		} catch (NoSuchMethodException e) { throw new RuntimeException(e); }
	}


	public void set(Properties properties) {
		this.properties=properties;
		super.set(properties);
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
			
			result.set(properties);
			items.put(name, result);
		}
		return  result;
	}


	@Override
	public String asString() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void set(String value) {
		// TODO Auto-generated method stub
		
	}
}
