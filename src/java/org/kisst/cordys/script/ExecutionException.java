package org.kisst.cordys.script;

import org.kisst.cordys.relay.SoapFaultException;



/** 
 * This Exception class is used to indicate an error in a Method definition.
 * 
 */
public class ExecutionException extends SoapFaultException {
	private static final long serialVersionUID = 1L;

	private final ExecutionContext context;

	public ExecutionException(ExecutionContext context, String faultstring) {
		super("Method.Execution.Error",faultstring);
		this.context=context;
	}
	
	public String getDetails() { return context.getTrace(); }

}	

