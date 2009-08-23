package org.kisst.cordys.util;

import java.lang.reflect.Constructor;

public class ReflectionUtil {

	public static Constructor getConstructor(Class cls, Class[] signature) {
		Constructor[] consarr = cls.getConstructors();
		for (int i=0; i<consarr.length; i++) {
			Class[] paramtypes = consarr[i].getParameterTypes();
			if (java.util.Arrays.equals(signature, paramtypes))
				return consarr[i];
		}
		return null;
	}
}
