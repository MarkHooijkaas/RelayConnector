package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;

import com.eibus.xml.nom.Node;

public class VarStep implements Step {
	private final String name;
    private final Expression expr;
    
	public VarStep(CompilationContext compiler, final int node) {
		name =Node.getAttribute(node, "name");
		compiler.declareTextVar(name);
		expr=ExpressionParser.parse(compiler, Node.getAttribute(node,	"value"));
	}

	public void executeStep(final ExecutionContext context) {
		String value=expr.getString(context);
		if (context.debugTraceEnabled())
			context.traceDebug("setting text var "+name+" to value "+value );
		context.setTextVar(name, value);
	}
}
