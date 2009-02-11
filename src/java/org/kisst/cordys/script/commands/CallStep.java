package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;

import com.eibus.xml.nom.Node;

public class CallStep extends MethodCall implements Step {
    
	public CallStep(CompilationContext compiler, final int node) {
		super(compiler, node);
	}
	
	public void executeStep(final ExecutionContext context) {
		int method=0;
		try {
			method=this.createMethod(context);
			this.callMethod(context, method);
		}
		finally {
			if (method!=0)
				Node.delete(Node.getParent(Node.getParent(method)));
		}
	}
}
