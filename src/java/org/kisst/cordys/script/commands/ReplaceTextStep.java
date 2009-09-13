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
import org.kisst.cordys.script.expression.XmlExpression;

import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.NodeType;

public class ReplaceTextStep implements Step {
	private final XmlExpression start;
	private final String elementsNamed;
	private final Expression expr;
	
	public ReplaceTextStep(CompilationContext compiler, final int node) {
		start=new XmlExpression(compiler, Node.getAttribute(node, "start"));
		elementsNamed = Node.getAttribute(node, "elementsNamed");
		compiler.declareTextVar("it"); // TODO: remove after compilation
		expr=ExpressionParser.parse(compiler, Node.getAttribute(node, "expression"));
	}

	public void executeStep(ExecutionContext context) {
		int node=start.getNode(context);
		replace(context, node);
	}

	private void replace(ExecutionContext context, int node) {
		if (Node.getType(node)!=NodeType.ELEMENT)
			return;
		if (elementsNamed==null || elementsNamed.equals(Node.getLocalName(node))) {
			context.setTextVar("it", Node.getData(node));
			String newValue=expr.getString(context);
			if (context.debugTraceEnabled())
				context.traceDebug("replacing text from "+Node.getName(node)+" to value "+newValue);
			Node.setDataElement(node, "", newValue);
		}
		if (elementsNamed!=null) {
			int child=Node.getFirstElement(node);
			while (child!=0) {
				replace(context,child);
				child=Node.getNextSibling(child);
			}
		}
	}
}
