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
