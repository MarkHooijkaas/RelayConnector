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
import org.kisst.cordys.util.NomPath;
import org.kisst.cordys.util.NomUtil;

import com.eibus.xml.nom.Node;

public class ElementAppender implements XmlAppender {
	private final String name;
	private final String namespace;
	private final String prefix;
	private final XmlAppender[] parts;
	private final boolean reduceXmlns;
	private final boolean resolveXmlns;
	private final NomPath target;

	public ElementAppender(CompilationContext compiler, int node) {
		this.name=Node.getAttribute(node,"name");
		this.prefix=Node.getAttribute(node,"prefix");
		this.resolveXmlns=NomUtil.getBooleanAttribute(node, "resolveXmlns", true);
		this.reduceXmlns=NomUtil.getBooleanAttribute(node, "reduceXmlns", true);
		if (Node.getAttribute(node,"target")==null)
			target=null;
		else
			target=new NomPath(compiler, Node.getAttribute(node,"target"));

		String namespaceAttr=Node.getAttribute(node,"namespace");
		if (prefix!=null && namespaceAttr==null && resolveXmlns)
			this.namespace=compiler.resolvePrefix(prefix);
		else
			this.namespace=namespaceAttr;

		// First count the number of parts, so that the array can be size precisely
		int partCount=Node.getNumChildren(node);
		if (attributeExists(node, "text"))
			partCount++;
		// if a xml or childrenOf attribute exists, a include alement will be added
		if (attributeExists(node,"xml")|| attributeExists(node,"childrenOf"))
			partCount++;

		parts=new XmlAppender[partCount];

		
		int index=0;
		if (attributeExists(node, "text"))
			// pass an empty name
			parts[index++]=new TextAppender(compiler, node, null);
		if (attributeExists(node,"xml")|| attributeExists(node,"childrenOf"))
			parts[index++]=new IncludeAppender(compiler, node);
		

		int child=Node.getFirstElement(node);
		while (child!=0) {
			String nodeName=Node.getLocalName(child);
			if ("element".equals(nodeName))
				parts[index]=new ElementAppender(compiler,child);
			else if ("attribute".equals(nodeName))
				parts[index]=new AttributeAppender(compiler, child);
			else if ("text".equals(nodeName))
				parts[index]=new TextAppender(child, compiler);
			else if ("cdata".equals(nodeName))
				parts[index]=new CDataAppender(compiler, child);
			else if ("include".equals(nodeName)) {
				parts[index]=new IncludeAppender(compiler, child);
			}
			else 
				throw new RuntimeException("unknown xml subelement "+nodeName);
			index++;
			child=Node.getNextSibling(child);
		}
	}

	public void append(ExecutionContext context, int toNode) {
		if (target!=null)
			toNode=target.findNode(toNode);
		if (name!=null)
			toNode=Node.createElement(name, toNode);
		if (namespace!=null)
			NomUtil.setNamespace(toNode,namespace,prefix,reduceXmlns);
		else {
			if (prefix!=null)
				Node.setName(toNode, prefix+":"+Node.getLocalName(toNode));
		}			
		for(XmlAppender e:parts)
			e.append(context, toNode);
	}

	
	private boolean attributeExists(int node, String name) {
		return Node.getAttribute(node, name)!=null;
	}
}
