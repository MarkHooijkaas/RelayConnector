package org.kisst.cordys.script.commands;

import org.kisst.cordys.relay.SoapFaultException;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;

import com.eibus.xml.nom.Node;

public class FaultStep implements Step {
	private final String code;
	private final Expression messageExpression;
	
	
	public FaultStep(CompilationContext compiler, final int node) {
		code = Node.getAttribute(node, "code");
		messageExpression = ExpressionParser.parse(compiler, Node.getAttribute(node, "message"));
	}

	public void executeStep(ExecutionContext context) {
		throw new SoapFaultException(code, messageExpression.getString(context));
	}
}
