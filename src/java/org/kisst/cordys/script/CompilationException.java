package org.kisst.cordys.script;

import org.kisst.cordys.relay.SoapFaultException;

import com.eibus.xml.nom.Node;



/** 
 * This Exception class is used to indicate an error in a Script compilation.
 * 
 */
public class CompilationException extends SoapFaultException {
	private static final long serialVersionUID = 1L;

	private final CompilationContext compiler;

	public CompilationException(CompilationContext compiler, String faultstring, Throwable e) {
		super("Method.Compilation.Error",faultstring, e);
		this.compiler=compiler;
	}

	public CompilationException(CompilationContext compiler, String faultstring) {
		super("Method.Compilation.Error",faultstring);
		this.compiler=compiler;
	}

	@Override
	protected boolean hasDetails()     { return compiler.traceAvailable(); }

	@Override
	protected void fillDetails(int node) {
		if (! hasDetails())
			return;
		int traceNode=Node.createElement("trace", node);
		compiler.addToNode(traceNode);
	}

}	

