/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
*/

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
    		script=new Script(node, compiler.getProps());
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
