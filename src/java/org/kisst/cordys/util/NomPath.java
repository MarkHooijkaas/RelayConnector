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

package org.kisst.cordys.util;

import java.util.ArrayList;
import java.util.List;

import org.kisst.cordys.script.PrefixContext;

import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.NodeType;

public class NomPath {
	private class Part {
		boolean optional=false;
		boolean isAttribute=false;
		boolean isText=false;
		boolean isParent=false;
		boolean singlestar=false;
		boolean isLast=false;
		boolean isDot=false;
		boolean isLocalname=false;
		boolean isName=false;
		boolean isNamespace=false;
		boolean searchLast=false;
		boolean searchFirst=false;
		String name;
		String namespace=null;

	}
	private final Part[] parts;
	private final String original;
	private final boolean stringResult;
	private final boolean singleResult;

	public String toString() { return original;}

	public boolean singleResult() { return singleResult;}
	public boolean stringResult() { return stringResult;}

	public NomPath(PrefixContext prefixContext, String str) {
		this(prefixContext, str, false);
	}
	
	public NomPath(PrefixContext prefixContext, String str, boolean optional) {
		boolean alwaysSingle=true;
		boolean nodeResult=true;
		original=str;
		while (str.endsWith("/"))
			str=str.substring(0, str.length()-1);
		while (str.startsWith("/"))
			str=str.substring(1);

		if (str.trim().length()==0)
			parts=null;
		else {
			String[] elements=str.split("/");
			parts=new Part[elements.length];
			for(int i=0; i<elements.length; i++) {
				String e=elements[i];
				parts[i]=new Part();
				parts[i].optional=optional;
				if (i==parts.length-1)
					parts[i].isLast=true;
				if (e.startsWith("?")) {
					parts[i].optional=true;
					e=e.substring(1);
				}
				if (e.endsWith("$")) {
					parts[i].searchLast=true;
					e=e.substring(0,e.length()-1);
				}
				if (e.startsWith("^")) {
					parts[i].searchFirst=true;
					e=e.substring(1);
				}
				if (".".equals(e)) {
					parts[i].isDot=true;
					continue;
				}
				if ("..".equals(e)) {
					parts[i].isParent=true;
					continue;
				}
				if ("*".equals(e)) {
					parts[i].singlestar=true;
					parts[i].optional=true;
					alwaysSingle=false;
					continue;
				}
				if ("text()".equals(e)) {
					if (! parts[i].isLast)
						throw new RuntimeException("text() should be last element in path "+original);
					parts[i].isText=true;
					nodeResult=false;
					continue;
				}
				if ("name()".equals(e)) {
					if (! parts[i].isLast)
						throw new RuntimeException("name() should be last element in path "+original);
					parts[i].isName=true;
					nodeResult=false;
					continue;
				}
				if ("localname()".equals(e)) {
					if (! parts[i].isLast)
						throw new RuntimeException("localname() should be last element in path "+original);
					parts[i].isLocalname=true;
					nodeResult=false;
					continue;
				}
				if ("namespace()".equals(e)) {
					if (! parts[i].isLast)
						throw new RuntimeException("namespace() should be last element in path "+original);
					parts[i].isNamespace=true;
					nodeResult=false;
					continue;
				}
				if (e.startsWith("@")) {
					if (! parts[i].isLast)
						throw new RuntimeException("Attribute @ should be last element in path "+original);
					parts[i].isAttribute=true;
					nodeResult=false;
					e=e.substring(1);
				}
				if (e.startsWith("*")) {
					parts[i].singlestar=true;
					parts[i].optional=true;
					alwaysSingle=false;
					e=e.substring(1);
				}
				int pos=e.indexOf(":");
				if (pos>=0) {
					parts[i].namespace=prefixContext.resolvePrefix(e.substring(0,pos));
					parts[i].name=e.substring(pos+1);
				}
				else
					parts[i].name=e;
			}
		}
		singleResult = alwaysSingle;
		this.stringResult=! nodeResult;
	}

	public String getText(int node) {
		// TODO: better handling of an attribute
		node=findNode(node);
		Part last=parts[parts.length-1];
		if (last.isAttribute)
			return Node.getAttribute(node, last.name);
		if (last.isName)
			return Node.getName(node);
		if (last.isNamespace)
			return Node.getNamespaceURI(node);
		if (last.isLocalname)
			return Node.getLocalName(node);
		return Node.getData(node);
	}

	public int setText(int node, String text) {
		node=findNodeWithCreate(node);
		Part last=parts[parts.length-1];
		if (last.isAttribute) {
			Node.setAttribute(node, last.name, text);
			return 0;
		}
		else
			Node.setDataElement(node, "", text);
		return node;
	}

	public int findNode(int node) {
		if (parts==null)
			return node;
		for(int i=0; i<parts.length; i++) {
			Part part=parts[i];
			String name = part.name;
			if (part.isDot) {
				// do nothing
			}
			else if (part.isParent)
				node=Node.getParent(node);
			else if (part.isAttribute || part.isText || part.isName || part.isNamespace || part.isLocalname) {
				// skip for an attribute or text node: should only happen for final node
			}
			else {
				if (name==null || name.length()==0) { 
					if (part.searchLast) {
						node=Node.getLastChild(node); 
						while (node!=0 && Node.getType(node)!=NodeType.ELEMENT)
							node=Node.getPreviousSibling(node);
					}
					else
						node=Node.getFirstElement(node);
				}
				else if (part.namespace==null) {
					if (part.searchLast)
						node=NomUtil.getLastElementByLocalName(node, name);
					else
						node=NomUtil.getElementByLocalName(node, name);
				}
				else
					node=NomUtil.getElement(node, part.namespace, name);
				if (node==0 && ! part.optional)
					throw new RuntimeException("non-optional element "+name+" does not exist in Path expression "+original);
			}
		}
		return node;
	}


	public List<NomNode> getNodeList(int node) {
		ArrayList<NomNode> result=new ArrayList<NomNode>();
		fillNodeList(node,0,result,false);
		return result;
	}
	public List<String> getTextList(int node) {
		List<NomNode> nodes = getNodeList(node);
		ArrayList<String> result=new ArrayList<String>();
		String attrName=null;
		Part last=parts[parts.length-1];
		if (last.isAttribute)
			attrName=last.name;
		for(NomNode n:nodes) {
			if (attrName==null)
				result.add(n.getText());
			else
				result.add(Node.getAttribute(n.node, attrName));
		}
		return result;
	}

	private void fillNodeList(int node, int index, List<NomNode> result, boolean superstar) {
		if (parts==null || index>=parts.length)
			return;
		Part part=parts[index];
		if (part.isParent)
			fillNodeList(Node.getParent(node), index+1, result, superstar);
		else if (part.isAttribute)
			result.add(new NomNode(node));
		else if (part.isText)
			result.add(new NomNode(node));
		else {
			int child=Node.getFirstElement(node);
			boolean atLeastOneMatch=false;
			while (child!=0) {
				boolean match=false;
				if (Node.getType(child)==NodeType.ELEMENT) {
					if (part.name==null) {
						if (part.namespace==null)
							match=true;
						else if (part.namespace.equals(Node.getNamespaceURI(child)))
							match=true;
					}
					else if (Node.getLocalName(child).equals(part.name)) {
						if (part.namespace==null)
							match=true;
						else if (part.namespace.equals(Node.getNamespaceURI(child)))
							match=true;
					}
				}
				if (match) {
					atLeastOneMatch=true;
					if (part.isLast)
						result.add(new NomNode(child));
					else
						fillNodeList(child, index+1, result, superstar);
				}
				if (part.singlestar || ! match)
					child=Node.getNextSibling(child);
				else
					child=0;
			}
			if (! (atLeastOneMatch || part.optional || superstar))
				// Add namespace and star info in error
				throw new RuntimeException("Could not find any non-optional element with name "+part.name);
		}
	}
	public int findNodeWithCreate(int node) {
		if (parts==null)
			return 0;
		for(int i=0; i<parts.length; i++) {
			Part part=parts[i];
			String name = part.name;
			if (part.isDot) {
				// do nothing
			}
			else if (part.isParent)
				node=Node.getParent(node);
			else if (part.isAttribute) {
				// skip for an attribute: should only happen for final node
			}
			else if (part.searchLast) {
				int nodefound=NomUtil.getLastElementByLocalName(node, name);
				if (nodefound==0)
					node=Node.createElement(name, node);
				else
					node=nodefound;
			}
			else if (part.searchFirst) {
				int nodefound=NomUtil.getElementByLocalName(node, name);
				if (nodefound==0)
					node=Node.createElement(name, node);
				else
					node=nodefound;
			}

/*			else if (part.optional) {
				node=NomUtil.getLastElementByLocalName(node, name);
				if (node==0)
					return 0;
			}
		*/
			else
				node=Node.createElement(name, node);
		}
		return node;
	}
}