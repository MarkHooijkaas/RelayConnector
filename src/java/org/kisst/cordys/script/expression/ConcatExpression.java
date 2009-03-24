package org.kisst.cordys.script.expression;

import java.util.regex.Pattern;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;

public class ConcatExpression implements Expression {
	private static Pattern splitter = Pattern.compile("[+]");

	private final Expression[] expressions;
	
	public ConcatExpression(CompilationContext compiler, final String str) {
		String[] parts=splitter.split(str);
		expressions = new Expression[parts.length];
		for(int i=0; i<parts.length; i++) {
			expressions[i]=ExpressionParser.parse(compiler,parts[i]);
		}
	}

	public String getString(ExecutionContext context) {
		String result = expressions[0].getString(context);
		for (int i=1; i<expressions.length; i++)
			result += expressions[i].getString(context);
		return result;
	}

}
