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

package org.kisst.cordys.script.xml;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;

import com.eibus.xml.nom.Node;


public class AttributeAppender implements XmlAppender {
	private final String name;
	private final Expression textExpression;
	AttributeAppender(CompilationContext compiler, int node) {
		this.name=Node.getAttribute(node, "name");

		String text=Node.getAttribute(node, "text");
		textExpression = ExpressionParser.parse(compiler, text);
		if (name==null)
			throw new RuntimeException("attribute 'name' should be set in when attribute text is set");
	}
	public void append(ExecutionContext context, int toNode) {
		Node.setAttribute(toNode, name, textExpression.getString(context));			
	}
}
