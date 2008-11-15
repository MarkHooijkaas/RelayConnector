package org.kisst.cordys.script.xml;

import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.CompilationContext;
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
