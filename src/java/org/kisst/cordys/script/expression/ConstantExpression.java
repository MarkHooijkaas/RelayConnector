package org.kisst.cordys.script.expression;

import org.kisst.cordys.script.ExecutionContext;

public class ConstantExpression implements Expression {
	private final String str;
	
	public ConstantExpression(String str) {
		this.str=str;
	}

	public String getString(ExecutionContext context) {
		return str;
	}

}
