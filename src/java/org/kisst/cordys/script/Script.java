/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
*/

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

