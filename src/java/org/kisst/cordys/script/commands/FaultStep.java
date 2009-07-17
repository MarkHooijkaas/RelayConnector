package org.kisst.cordys.script.commands;

import org.kisst.cordys.relay.SoapFaultException;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;

import com.eibus.xml.nom.Node;

public class FaultStep implements Step {
	private final Expression codeExpression;
	private final Expression messageExpression;
	
	
	public FaultStep(CompilationContext compiler, final int node) {
		codeExpression = ExpressionParser.parse(compiler, Node.getAttribute(node, "code"));
		messageExpression = ExpressionParser.parse(compiler, Node.getAttribute(node, "message"));
	}

	public void executeStep(ExecutionContext context) {
		String msg=messageExpression.getString(context);
		String code=codeExpression.getString(context);
		if (context.debugTraceEnabled())
			context.traceDebug("throwing SoapFaulException "+code+": "+msg);
		throw new SoapFaultException(code, msg);
	}
}
