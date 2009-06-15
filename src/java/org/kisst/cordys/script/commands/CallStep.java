package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;

public class CallStep extends MethodCall implements Step {
    
	public CallStep(CompilationContext compiler, final int node) {
		super(compiler, node);
	}
	
	public void executeStep(final ExecutionContext context) {
		int method=this.createMethod(context);
		this.callMethod(context, method);
	}
}
