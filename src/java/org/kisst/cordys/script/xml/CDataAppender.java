package org.kisst.cordys.script.xml;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.expression.ConstantExpression;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;

import com.eibus.xml.nom.Node;

public class CDataAppender implements XmlAppender {
	private final String name;
	private final Expression textExpression;
	public CDataAppender (CompilationContext compiler, int node) {
		name=Node.getAttribute(node,"name");
		String text=Node.getAttribute(node, "text");
		if (text!=null)
			textExpression = ExpressionParser.parse(compiler, text);
		else
			textExpression = new ConstantExpression(Node.getData(node));
	}
	public void append(ExecutionContext context, int toNode) {
		if (name!=null) 
			Node.createCDataElement(name, textExpression.getString(context), toNode);
		else {
			int cdata=Node.getDocument(toNode).createCData(textExpression.getString(context));
			Node.appendToChildren(cdata, toNode);
		}
	}
}
