package org.kisst.cordys.script;

import org.kisst.cordys.relay.CallContext;
import org.kisst.cordys.util.NomUtil;

import com.eibus.xml.nom.Node;

public class Script implements Step {
	private final CallContext ctxt;
	private final CompilationContext compiler;
	private final Step[] steps;
	
	public CallContext getCallContext() { return ctxt; } 

	public Script(CallContext ctxt, int scriptNode) {
		this.ctxt=ctxt;
		this.compiler = new CompilationContext(ctxt,this);
		this.steps = new Step[Node.getNumChildren(scriptNode)];
		compile(compiler, scriptNode);
	}

	public Script(CompilationContext compiler, int scriptNode) {
		this.ctxt=compiler.getCallContext();
		this.compiler = compiler;
		this.steps = new Step[Node.getNumChildren(scriptNode)];
		compile(compiler, scriptNode);
	}
	
	protected void compile(CompilationContext compiler, int scriptNode) {
		int i=0;
    	int node = Node.getFirstChild(scriptNode);
    	while (node != 0) {
    		try {
    			if (ctxt.debugTraceEnabled())
    				ctxt.traceDebug("compiling "+NomUtil.nodeToString(node));
    			steps[i]=compiler.compile(node);
    		}
    		catch (Exception e) {
    			String s = NomUtil.nodeToString(node);
    			Throwable t=e;
    			while (t.getCause()!=null)
    				t=t.getCause();
    			throw new CompilationException(compiler, "Error when parsing "+s+", original error: "+t.getMessage(),e);
    		}
    		node = Node.getNextSibling(node);
    		i++;
    	}
	}

	public void executeStep(ExecutionContext context) {
		for (int i=0; i<steps.length; i++) {
			if (steps[i]!=null) {// skip null steps 
				if (context.debugTraceEnabled())
					context.traceDebug("executing "+steps[i].getClass().getSimpleName());
				steps[i].executeStep(context);
			}
		}
	}
}

