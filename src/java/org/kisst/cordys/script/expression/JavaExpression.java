package org.kisst.cordys.script.expression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;

public class JavaExpression implements Expression {
	private final Method method;
	private final Expression paramExpressions[];

	public JavaExpression(CompilationContext compiler, String str) {
		// TODO: more robust parsing, e.g. nested java calls
		int pos=str.indexOf("::");
		int pos2=str.indexOf("(", pos);
		int pos3=str.lastIndexOf(")");
		String className=str.substring(0,pos).trim();
		String methodName=str.substring(pos+2,pos2).trim();
		String params[]=str.substring(pos2+1,pos3).split(",");
		Class paramTypes[]=new Class[params.length];
		for (int i=0; i<params.length; i++)
			paramTypes[i]=String.class;
		try {
			Class<?> cls=Class.forName(className);
			method=cls.getMethod(methodName, paramTypes);
		} 
		catch (ClassNotFoundException e) { 
			throw new RuntimeException("ne class named "+className
					+" found while parsing java expression "+str, e);
		} 
		catch (NoSuchMethodException e) {  
			throw new RuntimeException("no method found with name "+methodName
					+" and with "+params.length+" string arguments "
					+ " while parsing java expression "+str, e); 
		}
		if (! method.getReturnType().equals(String.class))  {  
			throw new RuntimeException("method "+methodName
					+" with "+params.length+" string arguments does not return a String"
					+ " while parsing java expression "+str); 
		}
		if (! Modifier.isStatic(method.getModifiers()))  {  
			throw new RuntimeException("method "+methodName
					+" with "+params.length+" string arguments does not return a String"
					+ " while parsing java expression "+str); 
		}
		
		paramExpressions = new Expression[params.length];
		for (int i=0; i<params.length; i++)
			paramExpressions[i] = ExpressionParser.parse(compiler, params[i]);
	}

	public String getString(ExecutionContext context) {
		Object args[]=new Object[paramExpressions.length];
		for (int i=0; i<args.length; i++)
			args[i]=paramExpressions[i].getString(context);
		Object result;
		try {
			result = method.invoke(null, args);
		}
		catch (IllegalArgumentException e) { throw new RuntimeException("invocation error ",e); }
		catch (IllegalAccessException e) { throw new RuntimeException("invocation error ",e); }
		catch (InvocationTargetException e) { throw new RuntimeException("invocation error ",e); }
		
		return (String) result;
	}

}
