package org.kisst.cordys.script.expression;

import org.codehaus.groovy.control.CompilationFailedException;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

public class GroovyExpression implements Expression {
	private static long count=1;
	private final static GroovyClassLoader loader=new GroovyClassLoader();
	
	private final GroovyObject expression;
	public GroovyExpression(CompilationContext compiler, String str) {
		try {
			// The expression is turned into a mini class. 
			// This seems to be the only way that a value can be returned.
			String script="class Expression"+count+" { Object eval() { return "+str+";}}";
			expression=	(GroovyObject) loader.parseClass(script).newInstance();
		} 
		catch (CompilationFailedException e) { throw new RuntimeException(e);}
		catch (InstantiationException e) { throw new RuntimeException(e);}
		catch (IllegalAccessException e) { throw new RuntimeException(e);}
	}

	public String getString(ExecutionContext context) {
		String[] args=new String[0];
		return ""+expression.invokeMethod("eval", args);
	}

}
