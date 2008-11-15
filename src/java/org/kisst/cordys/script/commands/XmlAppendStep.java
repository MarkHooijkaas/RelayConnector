package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.script.xml.ElementAppender;

import com.eibus.xml.nom.Node;

public class XmlAppendStep implements Step {
	private final XmlExpression dest;
    private final ElementAppender appender;
    
	public XmlAppendStep(CompilationContext compiler, final int node) {
		dest = new XmlExpression(compiler, Node.getAttribute(node, "to"));
		appender = new ElementAppender(compiler, node);
	}

	public void executeStep(final ExecutionContext context) {
		appender.append(context, dest.getNode(context));
	}
}
