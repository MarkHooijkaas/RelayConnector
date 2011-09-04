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
		String value=context.getProps().getString(key,null);
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
