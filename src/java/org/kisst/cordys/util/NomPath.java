package org.kisst.cordys.util;

import java.util.ArrayList;

import org.kisst.cordys.script.PrefixContext;

import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.NodeType;

public class NomPath {
	private class Part {
		boolean optional=false;
		boolean isAttribute=false;
		boolean isText=false;
		boolean isParent=false;
		boolean superstar=false;
		boolean singlestar=false;
		boolean isLast=false;
		String name;
		String namespace=null;

	}
	private final Part[] parts;
	private final String original;
	private boolean alwaysSingle=true;

	public NomPath(PrefixContext prefixContext, String str) {
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
				if (i==parts.length-1)
					parts[i].isLast=true;
				if (e.startsWith("?")) {
					parts[i].optional=true;
					e=e.substring(1);
				}
				if ("..".equals(e)) {
					parts[i].isParent=true;
					continue;
				}
				if ("**".equals(e)) {
					parts[i].superstar=true;
					alwaysSingle=false;
					continue;
				}
				if ("*".equals(e)) {
					parts[i].singlestar=true;
					parts[i].optional=true;
					alwaysSingle=false;
					continue;
				}
				if ("+".equals(e)) {
					parts[i].singlestar=true;
					alwaysSingle=false;
					continue;
				}
				if ("text()".equals(e)) {
					if (! parts[i].isLast)
						throw new RuntimeException("text() should be last element in path "+original);
					parts[i].isText=true;
					continue;
				}
				if (e.startsWith("@")) {
					if (! parts[i].isLast)
						throw new RuntimeException("Attribute @ should be last element in path "+original);
					parts[i].isAttribute=true;
					e=e.substring(1);
				}
				if (e.startsWith("*")) {
					parts[i].singlestar=true;
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

	}

	public String getText(int node) {
		// TODO: better handling of an attribute
		node=findNode(node);
		Part last=parts[parts.length-1];
		if (last.isAttribute)
			return Node.getAttribute(node, last.name);
		return Node.getData(node);
	}

	public int findNode(int node) {
		if (parts==null)
			return node;
		for(int i=0; i<parts.length; i++) {
			Part part=parts[i];
			String name = part.name;
			if (part.isParent)
				node=Node.getParent(node);
			else if (part.isAttribute || part.isText) {
				// skip for an attribute or text node: should only happen for final node
			}
			else {
				if (part.namespace==null)
					node=NomUtil.getElementByLocalName(node, name);
				else
					node=NomUtil.getElement(node, part.namespace, name);
			}
		}
		return node;
	}

	public void fillNodeList(int node, int index, ArrayList<Object> result, boolean superstar) {
		if (parts==null || index>=parts.length)
			return;
		Part part=parts[index];
		if (part.isParent)
			fillNodeList(Node.getParent(node), index+1, result, superstar);
		else if (part.isAttribute)
			result.add(Node.getAttribute(node, part.name));
		else if (part.isText)
			result.add(Node.getData(node));
		else if (part.superstar) {
			throw new RuntimeException("** not yet implemented in path "+original);
		}
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
						result.add(child);
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

	public boolean isAlwaysSingle() { return alwaysSingle;}
}