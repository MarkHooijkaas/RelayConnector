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