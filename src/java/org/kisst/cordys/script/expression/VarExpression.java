package org.kisst.cordys.script.expression;

import org.kisst.cordys.relay.SoapFaultException;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;

public class VarExpression implements Expression {
	private final String name;
	private final String varname;
	private final String defaultValue;
	private final String value;
	
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
		int pos=name.indexOf("?:");
		if (pos>0) {
			varname=name.substring(0,pos);
			defaultValue=name.substring(pos+2);
		}
		else {
			varname=name;
			defaultValue=null;
		}
		if (compiler.textVarExists(varname))
			value=null; // null indicates it still needs to be determined from a variable
		else {
			String configvalue=compiler.getRelayConnector().conf.get(varname, null);
			// Note: if compiled script is cached, and configuration is reloaded,
			// the script cache is cleared, so it is safe to remember here, even if script is cached
			if (configvalue!=null)
				value=configvalue;
			else if ("plus".equals(varname))
				value="+";
			else if (defaultValue!=null)
				value=defaultValue;
			else
				throw new SoapFaultException("UnknownVariable", "Variable expression ["+str+"] refers to non declared string variable,"+
				" which is also not in the configuration file");
		}
	}

	public String getString(ExecutionContext context) {
		if (value==null) {
			String result = context.getTextVar(name);
			if (result!=null)
				return result;
			if (defaultValue!=null)
				return defaultValue;
			throw new SoapFaultException("NullVariable","The variable with name "+varname+" had a null value");
		}
		else
			return value;
	}
}
