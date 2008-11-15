package org.kisst.cordys.script.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.GenericCommand;
import org.kisst.cordys.script.Step;

import com.eibus.xml.nom.Node;

public class ImportStep implements Step {
	public ImportStep(CompilationContext compiler, final int node) {
		String name = Node.getAttribute(node, "name");
		String className = Node.getAttribute(node, "class");
		Class<?> cls;
		try {
			cls=Class.forName(className);
		} 
		catch (ClassNotFoundException e) { throw new RuntimeException("error when importing class "+className,e);}
		if (name==null) {
			Method m;
			try {
				m=cls.getMethod("compile", new Class[]{ExecutionContext.class, int.class});
			}
			catch (NoSuchMethodException e) { throw new RuntimeException("imported class "+cls.getName()+"without name should have static method compile");}
			try {
				m.invoke(null, new Object[]{compiler, new Integer(node)});
			}
			catch (IllegalArgumentException e) {throw new RuntimeException("error importing class "+cls.getName());}
			catch (IllegalAccessException e) {throw new RuntimeException("error importing class "+cls.getName());}
			catch (InvocationTargetException e) {throw new RuntimeException("error importing class "+cls.getName());}
		}
		else {
			if (! Step.class.isAssignableFrom(cls))
				throw new RuntimeException("imported class "+cls.getName()+"with name "+name+" should implement the Step interface ");
			compiler.addCommand(name, new GenericCommand(cls));
		}
	}

	public void executeStep(ExecutionContext context) {
		// do nothing: import statement is only processed during compilation phase
	}
}
