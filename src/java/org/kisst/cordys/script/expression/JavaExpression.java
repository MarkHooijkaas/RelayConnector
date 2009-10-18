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

package org.kisst.cordys.script.expression;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.util.ReflectionUtil;

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
		return (String) ReflectionUtil.invoke(null, method, args);
	}

}
