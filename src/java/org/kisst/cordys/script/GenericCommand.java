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

package org.kisst.cordys.script;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class GenericCommand implements Command {
	private final Constructor<?> cons;

	public GenericCommand(final Class<?> cls) {
		try {
			cons=cls.getConstructor(CompilationContext.class, int.class);
		} catch (NoSuchMethodException e) { throw new RuntimeException("Not correct class",e); }
	}

	public Step compileStep(int node, CompilationContext compiler) {
		try {
			return (Step) cons.newInstance(new Object[] {compiler, new Integer(node)});
		}
		catch (IllegalArgumentException e) { throw new RuntimeException("Could not compile step for class"+cons.getDeclaringClass().toString(),e); }
		catch (InstantiationException e) { throw new RuntimeException("Could not compile step for class"+cons.getDeclaringClass().toString(),e); }
		catch (IllegalAccessException e) { throw new RuntimeException("Could not compile step for class"+cons.getDeclaringClass().toString(),e); }
		catch (InvocationTargetException e) { throw new RuntimeException("Could not compile step for class"+cons.getDeclaringClass().toString(),e); }
	}

}
