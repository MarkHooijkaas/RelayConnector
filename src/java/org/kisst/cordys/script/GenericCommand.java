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
