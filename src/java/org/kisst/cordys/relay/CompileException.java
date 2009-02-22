package org.kisst.cordys.relay;



/** 
 * This Exception class is used to indicate an error in a Method definition.
 * 
 */
public class CompileException extends SoapFaultException {
	private static final long serialVersionUID = 1L;

	public CompileException(String faultstring) {
		super("Method.Compile.Error",faultstring);
	}
}	

