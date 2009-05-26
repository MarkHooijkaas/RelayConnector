package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.RelayTrace;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.util.NomUtil;

import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class TraceStep implements Step {
	private final boolean traceCompilation;
	private final boolean traceExecution;
	private final Severity level;
	private final boolean newvalue;
	public TraceStep(CompilationContext compiler, final int node) {
		newvalue=NomUtil.getBooleanAttribute(node, "value", false);
		traceCompilation=NomUtil.getBooleanAttribute(node, "compilation", true);
		traceExecution=NomUtil.getBooleanAttribute(node, "execution", true);
		level = RelayTrace.parseSeverity(Node.getAttribute(node, "level", "DEBUG"));
		if (traceCompilation) {
			compiler.setTrace(level);
			compiler.traceDebug("Setting compilation trace to "+newvalue);
		}
	}

	public void executeStep(ExecutionContext context) {
		if (traceExecution) {
			context.setTrace(level);
			context.traceDebug("Setting execution trace to "+newvalue);
		}
	}
}
