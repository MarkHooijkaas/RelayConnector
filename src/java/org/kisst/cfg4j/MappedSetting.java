/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
*/

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
	
	public void reset() { items.clear(); }
}
