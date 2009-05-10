package org.kisst.cordys.script.expression;

import org.kisst.cordys.script.CompilationContext;


public class ExpressionParser {
	
	public static Expression parse(CompilationContext compiler, String str) {
		if (str==null)
			return null;
		str=str.trim();
		// TODO: much better parser: This will break in many situations
		// e.g. + embedded in strings, and many other nested scenarios
		if (str.startsWith("@groovy:"))
			return new GroovyExpression(compiler, str.substring(8));
		if (str.indexOf('+')>=0)
			return new ConcatExpression(compiler, str);
		if (str.startsWith("/"))
			return new XmlExpression(compiler, str);
		if (str.startsWith("${"))
			return new VarExpression(compiler, str);
		if (str.startsWith("["))
			return new ConstantExpression(str.substring(str.indexOf('[')+1, str.lastIndexOf(']')));
		if (str.indexOf("::")>0)
			return new JavaExpression(compiler,str);
		return new ConstantExpression(str);
	}
	
}
