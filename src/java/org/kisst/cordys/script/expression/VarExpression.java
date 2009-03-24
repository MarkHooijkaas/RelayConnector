package org.kisst.cordys.script.expression;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;

public class VarExpression implements Expression {
	private final String name;
	private String value=null;
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
		if (! compiler.textVarExists(name)) {
			value=compiler.getRelayConnector().conf.get(name, null);
			// Note: if compiled script is cached, and configuration is reloaded,
			// the script cache is cleared, so it is safe to remember here, even if script is cached
			if (value==null && "plus".equals(name))
				value="+";
			if (value==null)
				throw new RuntimeException("Variable expression ["+str+"] refers to non declared string variable,"+
				" which is also not in the configuration file");
		}
	}

	public String getString(ExecutionContext context) {
		if (value==null)
			return context.getTextVar(name);
		else
			return value;
	}
}
