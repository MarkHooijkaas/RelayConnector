package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class SoapMergeStep implements Step {
	private final XmlExpression srcExpression;
	private final XmlExpression destExpression;
	
	
	public SoapMergeStep(CompilationContext compiler, final int node) {
		srcExpression = new XmlExpression(compiler, Node.getAttribute(node, "src"));
		destExpression = new XmlExpression(compiler, Node.getAttribute(node, "dest"));
	}

	public void executeStep(ExecutionContext context) {
		int src  = srcExpression.getNode(context);
		int dest = destExpression.getNode(context);
		SoapUtil.mergeResponses(src,dest);
		if (context.debugTraceEnabled()) {
			if (context.getCallContext().getTrace()!=null)
				context.getCallContext().getTrace().trace(Severity.DEBUG, "result after soap merge ", dest);
		}
	}
}
