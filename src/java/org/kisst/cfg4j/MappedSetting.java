package org.kisst.cfg4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Properties;


public class MappedSetting<T extends Setting> extends Setting {
	private final Properties properties;
	private final HashMap<String,T> items=new HashMap<String,T>();
	private final Constructor constructor;
	
	public MappedSetting(Setting parent, String name, Properties props, Class<?> clazz) { 
		super(parent, name);
		this.properties=props;
		try {
			constructor=clazz.getConstructor(new Class[] {Setting.class, String.class, Properties.class});
		} catch (NoSuchMethodException e) { throw new RuntimeException(e); }
	}


	@SuppressWarnings("unchecked")
	public T get(String name) {
		T result=items.get(name);
		if (result==null) {
			try {
				result= (T) constructor.newInstance(new Object[] {this, name, properties});
			}
			catch (IllegalArgumentException e) { throw new RuntimeException(e); }
			catch (InstantiationException e) { throw new RuntimeException(e); }
			catch (IllegalAccessException e) { throw new RuntimeException(e); }
			catch (InvocationTargetException e) { throw new RuntimeException(e); }
			items.put(name, result);
		}
		return  result;
	}


	@Override
	public String asString() {
		// TODO Auto-generated method stub
		return null;
	}
}
