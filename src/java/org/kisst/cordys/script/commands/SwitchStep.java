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
    		script=new Script(compiler.getScript(), node);
    	}
    	private boolean evaluate(final ExecutionContext context, String casevalue) {
    		if (value!=null && ! value.equals(casevalue)) {
    			if (context.debugTraceEnabled()) 
   					context.traceDebug("case did not match for "+value );
    			return false;
    		}
			if (context.debugTraceEnabled()) {
				if (value==null)
					context.traceDebug("case match for otherwise");
				else
					context.traceDebug("case match for "+value );
			}
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
			boolean done=cases[i].evaluate(context, casevalue);
			if (done) {
				context.traceDebug("end switch");
				return;
			}
		}
		context.traceDebug("end switch without match");

	}
}
