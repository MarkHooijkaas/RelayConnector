package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.XmlExpression;

import com.eibus.xml.nom.Node;

public class GetConfigValueStep implements Step {
    private final Expression expr;
    private final String resultVar;
    
	public GetConfigValueStep(CompilationContext compiler, final int node) {
		expr=new XmlExpression(compiler, Node.getAttribute(node, "key"));
		resultVar=Node.getAttribute(node, "resultVar");
		compiler.declareTextVar(resultVar);
	}

	public void executeStep(final ExecutionContext context) {
		context.setTextVar(resultVar, context.getRelayConnector().conf.get(expr.getString(context)));
	}
}
