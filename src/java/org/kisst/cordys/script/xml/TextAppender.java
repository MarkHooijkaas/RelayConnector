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
import org.kisst.cordys.script.expression.ConstantExpression;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;

import com.eibus.xml.nom.Node;

public class TextAppender implements XmlAppender {
	private final String name;
	private final String expressionText;
	private final Expression textExpression;

	// Special constructor needed to ignore the name attribute 
	public TextAppender(CompilationContext compiler, int node, String name) {
		this.name=name;
		expressionText=Node.getAttribute(node, "text");
		if (expressionText!=null)
			textExpression = ExpressionParser.parse(compiler, expressionText);
		else
			textExpression = new ConstantExpression(Node.getData(node));
		
	}
	public TextAppender(int node, CompilationContext script) {
		this(script, node, Node.getAttribute(node,"name"));
	}
	public void append(ExecutionContext context, int toNode) {
		String text=textExpression.getString(context);
		if (text==null)
			// TODO: what would be correct behaviour if a null pointer is to be added? 
			text=""; // was: throw new RuntimeException("expression ["+expressionText+"] evaluated to null");
		if (name!=null) 
			Node.createTextElement(name, text, toNode);
		else 
			Node.setDataElement(toNode, "", text);
	}
}