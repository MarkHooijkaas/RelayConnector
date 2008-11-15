package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.XmlExpression;

import com.eibus.xml.nom.Node;

public class DeleteStep implements Step {
    private final XmlExpression expr;
    
	public DeleteStep(CompilationContext compiler, final int node) {
		String exprStr=Node.getAttribute(node,	"node");
		expr=new XmlExpression(compiler, exprStr);
	}

	public void executeStep(final ExecutionContext context) {
		Node.delete(expr.getNode(context));
	}
}
