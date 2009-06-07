package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.ExecutionException;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;

import com.eibus.xml.nom.Node;

public class GetConfigValueStep implements Step {
    private final Expression expr;
    private final String resultVar;
    private final Expression defaultValue;
    
	public GetConfigValueStep(CompilationContext compiler, final int node) {
		expr= ExpressionParser.parse(compiler, Node.getAttribute(node, "key"));
		resultVar=Node.getAttribute(node, "resultVar");
		compiler.declareTextVar(resultVar);
		String d=Node.getAttribute(node, "default");
		if (d==null)
			defaultValue=null;
		else
			defaultValue = ExpressionParser.parse(compiler, d);
	}

	public void executeStep(final ExecutionContext context) {
		String key=expr.getString(context);
		if (context.debugTraceEnabled())
			context.traceDebug("looking up config value"+key);
		String value=context.getProps().getString(key);
		if (value==null) {
			if (defaultValue==null)
				throw new ExecutionException(context, "Could not find config value ${"+key+"} and no default set");
			value=defaultValue.getString(context);
			if (context.debugTraceEnabled())
				context.traceDebug("using default value "+value);
		}
		if (context.debugTraceEnabled())
			context.traceDebug("setting "+resultVar+" to value "+value);
		context.setTextVar(resultVar, value);
	}
}
