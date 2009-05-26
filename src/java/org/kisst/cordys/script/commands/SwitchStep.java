package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Script;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;

import com.eibus.xml.nom.Node;

public class SwitchStep implements Step {
    private static class Case {
    	private final String value;
    	private final Script script;
    	private Case(CompilationContext compiler, int node) {
    		if ("otherwise".equals(Node.getLocalName(node)))
    			value=null;
    		else
    			value=Node.getAttribute(node, "value");
    		script=new Script(compiler, node);
    	}
    	private boolean evaluate(final ExecutionContext context, String casevalue) {
    		if (value!=null && ! value.equals(casevalue))
    			return false;
    		script.executeStep(context);
    		return true;
    	}
    }

    private final Expression expr;
    private final Case[] cases;

	public SwitchStep(CompilationContext compiler, final int node) {
		expr=ExpressionParser.parse(compiler, Node.getAttribute(node, "expression"));
		cases=new Case[Node.getNumChildren(node)];
		int child=Node.getFirstChild(node);
		int i=0;
		while (child!=0) {
			cases[i++]=new Case(compiler, child);
			child=Node.getNextSibling(child);
		}
		
	}

	public void executeStep(final ExecutionContext context) {
		String casevalue=expr.getString(context);
		if (context.debugTraceEnabled())
			context.traceDebug("switch on value "+casevalue );
		for (int i=0; i<cases.length; i++) {
			if (context.debugTraceEnabled())
				context.traceDebug("checking case "+cases[i].value );
			boolean done=cases[i].evaluate(context, casevalue);
			if (done) {
				context.traceDebug("case matched");
				return;
			}
		}
		context.traceDebug("end switch");

	}
}
