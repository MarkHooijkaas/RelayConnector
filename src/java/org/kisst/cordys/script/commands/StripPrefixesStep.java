package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.util.NomUtil;

import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.NodeType;


public class StripPrefixesStep implements Step {
    private final XmlExpression xml;
    private final XmlExpression childrenOf;
    private final boolean recursive;

	public StripPrefixesStep(CompilationContext compiler, int node) {
		String xmlStr=Node.getAttribute(node, "xml");
		if (xmlStr==null)
			xml=null;
		else
			xml=new XmlExpression(compiler, xmlStr);
		String childrenOfStr=Node.getAttribute(node, "childrenOf");
		if (childrenOfStr==null)
			childrenOf=null;
		else
			childrenOf=new XmlExpression(compiler, childrenOfStr);
		recursive=NomUtil.getBooleanAttribute(node, "recursive", true);
		if (xml==null && childrenOf==null)
			throw new RuntimeException("stripPrefixes step should have xml or childrenOf attribute");
		if (xml!=null && childrenOf!=null)
			throw new RuntimeException("stripPrefixes step should have just one of xml or childrenOf attributes");
	}

	public void executeStep(ExecutionContext context) {
		if (xml!=null)
			stripNode(context, xml.getNode(context));
		if (childrenOf!=null)
			stripChildren(context, childrenOf.getNode(context));
	}

	private void stripNode(ExecutionContext context, int node) {
		if (Node.getType(node)!=NodeType.ELEMENT)
			return;
		String name=Node.getName(node);
		int pos=name.indexOf(":");
		if (pos>=0) {
			if (context.debugTraceEnabled())
				context.traceDebug("Stripping prefix from node "+Node.getName(node));
			Node.setName(node, name.substring(pos+1));
		}
		if (recursive)
			stripChildren(context, node);
	}

	private void stripChildren(ExecutionContext context, int node) {
		if (Node.getType(node)!=NodeType.ELEMENT)
			return;
		node=Node.getFirstElement(node);
		while (node!=0) {
			stripNode(context, node);
			node=Node.getNextSibling(node);
		}
	}
}