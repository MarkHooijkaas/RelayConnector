package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.util.NomUtil;

import com.eibus.xml.nom.Node;

public class TraceStep implements Step {
	private final boolean traceCompilation;
	private final boolean traceExecution;
	private final boolean newvalue;
	public TraceStep(CompilationContext compiler, final int node) {
		newvalue=NomUtil.getBooleanAttribute(node, "value", false);
		String target = Node.getAttribute(node, "target");
		if ("all".equals(target) || target==null ) {
			traceCompilation=true;
			traceExecution=true;
		}
		else if ("compiler".equals(target)) {
			traceCompilation=true;
			traceExecution=false;
		}
		else if ("execution".equals(target)) {
			traceCompilation=false;
			traceExecution=true;
		}
		else
			throw new RuntimeException("opional target attribute should be all, compiler or execution");
		if (traceCompilation) {
			compiler.setDebugTrace(newvalue);
			compiler.traceDebug("Setting compilation trace to "+newvalue);
		}
	}

	public void executeStep(ExecutionContext context) {
		if (traceExecution) {
			context.setDebugTrace(newvalue);
			context.traceDebug("Setting execution trace to "+newvalue);
		}
	}
}
