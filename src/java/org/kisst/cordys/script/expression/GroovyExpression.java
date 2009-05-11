package org.kisst.cordys.script.expression;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;

public class GroovyExpression implements Expression {
	private final String exprName;

	public GroovyExpression(CompilationContext compiler, String str) {
		exprName=compiler.groovy.addExpression(str);
	}

	public String getString(ExecutionContext context) {
		return ""+context.evalGroovyExpression(exprName);
	}

}
