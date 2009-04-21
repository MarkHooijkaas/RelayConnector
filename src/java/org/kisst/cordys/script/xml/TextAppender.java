package org.kisst.cordys.script.xml;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.expression.ConstantExpression;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;

import com.eibus.xml.nom.Node;

public class TextAppender implements XmlAppender {
	private final String name;
	private final Expression textExpression;

	// Special constructor needed to ignore the name attribute 
	public TextAppender(CompilationContext compiler, int node, String name) {
		this.name=name;
		String text=Node.getAttribute(node, "text");
		if (text!=null)
			textExpression = ExpressionParser.parse(compiler, text);
		else
			textExpression = new ConstantExpression(Node.getData(node));
		
	}
	public TextAppender(int node, CompilationContext script) {
		this(script, node, Node.getAttribute(node,"name"));
	}
	public void append(ExecutionContext context, int toNode) {
		String text=textExpression.getString(context);
		if (name!=null) 
			Node.createTextElement(name, text, toNode);
		else 
			Node.setDataElement(toNode, "", text);
			//Node.getDocument(toNode).createText(text, toNode);
	}
}