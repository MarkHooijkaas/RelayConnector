package org.kisst.cordys.script.xml;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.expression.ExpressionParser;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.util.NomPath;

import com.eibus.xml.nom.Node;

public class IncludeAppender implements XmlAppender {
	private final XmlExpression xmlExpression;
	private final XmlExpression childrenOfExpression;
	private final NomPath target;

	/* TODO: figure out how I have planned this
	private final boolean recursive;
	private final String prefix;
	private final String prefixString;
	private final String namespace;
	 */
	
	public IncludeAppender(CompilationContext compiler, int node) {
		xmlExpression = (XmlExpression) ExpressionParser.parse(compiler, Node.getAttribute(node, "xml"));
		childrenOfExpression = (XmlExpression) ExpressionParser.parse(compiler, Node.getAttribute(node, "childrenOf"));
		if (Node.getAttribute(node,"target")==null)
			target=null;
		else
			target=new NomPath(compiler, Node.getAttribute(node,"target"));
		/*
		recursive = NomUtil.getBooleanAttribute(node, "recursive", true);
		prefix= Node.getAttribute(node, "prefix");
		if (prefix==null || prefix.equals("")) 
			prefixString="";
		else
			prefixString=prefix+":";
		namespace = Node.getAttribute(node, "namespace");
		*/
	}
	
	public void append(ExecutionContext context, int toNode) {
		if (target!=null)
			toNode=target.findNode(toNode);
		if (xmlExpression!=null) {
			int srcNode=xmlExpression.getNode(context);
			Node.duplicateAndAppendToChildren(srcNode, srcNode, toNode);
		}
		if (childrenOfExpression!=null) {
			int srcNode=childrenOfExpression.getNode(context);
			int firstChild = Node.getFirstChild(srcNode);
			int lastChild = Node.getLastChild(srcNode);
			Node.duplicateAndAppendToChildren(firstChild, lastChild, toNode);
		}
	}

	/*
	protected void deepCopyWithReplace(int fromNode, int toNode, int level) {
		//Document doc=Node.getDocument(toNode);
		String name=Node.getLocalName(fromNode);
		int newNode=Node.createElement(prefix+name, toNode);
		if (newNode==0 && prefix!=null) {
			// creation failed, possibly the prefix is not yet known
			// TODO: this is difficult to solve using current NOM routines.
			throw new RuntimeException("could not create a NOM element with name "+prefix+":"+name);
		}
		// copy attributes
		int attributeCount=Node.getNumAttributes(fromNode);
		for (int i=0; i<attributeCount; i++) {
			String attributeName=Node.getAttributeName(fromNode, i);
			Node.setAttribute(newNode, attributeName, Node.getAttribute(fromNode, attributeName));
		}
		if (level==0) {
			
		}
		// copy (and replace namespace) all children
		int child=Node.getFirstChild(newNode);
		
		while (child!=0) {
			if (Node.getType(child)== NodeType.ELEMENT)
				deepCopyWithReplace(child, toNode, level+1);
			else
				Node.clone(child, true);
			child = Node.getNextSibling(child);
		}
		
	}
	*/
}