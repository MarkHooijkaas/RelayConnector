package org.kisst.cordys.script;

import org.kisst.cordys.relay.SoapFaultException;



/** 
 * This Exception class is used to indicate an error in a Method definition.
 * 
 */
public class CompilationException extends SoapFaultException {
	private static final long serialVersionUID = 1L;

	private final CompilationContext compiler;

	public CompilationException(CompilationContext compiler, String faultstring) {
		super("Method.Compile.Error",faultstring);
		this.compiler=compiler;
	}
	
	public String getDetails() { return compiler.getTrace(); }

}	

