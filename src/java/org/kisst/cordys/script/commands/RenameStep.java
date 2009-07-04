package org.kisst.cordys.script.commands;

import java.util.List;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.util.NomNode;

import com.eibus.xml.nom.Node;

public class RenameStep implements Step {
	private final XmlExpression nodes;
	private final String name;
	private final String namespace;
	private final String prefix;
	
	public RenameStep(CompilationContext compiler, final int node) {
		nodes=new XmlExpression(compiler, Node.getAttribute(node, "nodes"));
		compiler.declareTextVar("it"); // TODO: remove after compilation
		name=Node.getAttribute(node, "name");
		prefix=Node.getAttribute(node, "prefix");
		namespace= compiler.getCallContext().resolvePrefix(prefix, Node.getAttribute(node, "namespace"));
	}

	public void executeStep(ExecutionContext context) {
		List<NomNode> nodeList=nodes.getNodeList(context);
		for (NomNode n: nodeList) {
			if (name!=null)
				n.rename(name);
			if (namespace!=null || prefix !=null)
				n.setNamespace(namespace, prefix);
		}
	}
}
