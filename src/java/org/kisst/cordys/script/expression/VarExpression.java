package org.kisst.cordys.script.expression;

import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.CompilationContext;

public class VarExpression implements Expression {
	private final String name;
	private final boolean useVar;
	public VarExpression(CompilationContext compiler, String str) {
		str=str.trim();
		if (str.startsWith("${")) {
			if (str.endsWith("}"))
				name=str.substring(2,str.length()-1);
			else
				throw new RuntimeException("Variable expression ["+str+"] should end with a } character");
		}
		else
			name=str;
		if (compiler.textVarExists(name))
			useVar=true;
		else if (compiler.getRelayConnector().conf.get(name, null)!=null)
			// Note: if compiled script is cached, and configuration is reloaded,
			// then script cache is cleared.
			useVar=false;
		else
			throw new RuntimeException("Variable expression ["+str+"] refers to non declared string variable,"+
					" which is also not in the configuration file");
	}

	public String getString(ExecutionContext context) {
		if (useVar)
			return context.getTextVar(name);
		else
			return context.getRelayConnector().conf.get(name);
	}
}
