package org.kisst.cordys.util;

import org.kisst.cordys.script.PrefixContext;

import com.eibus.xml.nom.Node;

public class NomPath {
	private final String[] elements;
	private final String[] namespaces;

	public NomPath(PrefixContext prefixContext, String str) {
		while (str.endsWith("/"))
			str=str.substring(0, str.length()-1);
		while (str.startsWith("/"))
			str=str.substring(1);

		if (str.trim().length()==0) {
			elements=null;
			namespaces=null;
		}
		else {
			elements=str.split("/");
			namespaces=new String[elements.length];
			for(int i=0; i<elements.length; i++) {
				int pos=elements[i].indexOf(":");
				if (pos>=0) {
					namespaces[i]=prefixContext.resolvePrefix(elements[i].substring(0,pos));
					elements[i]=elements[i].substring(pos+1);
				}
			}
		}

	}

	public String getText(int node) {
		// TODO: better handling of an attribute
		node=findNode(node);
		String name=elements[elements.length-1];
		if (name.startsWith("@"))
			return Node.getAttribute(node, name.substring(1));
		return Node.getData(node);
	}
	
	public int findNode(int node) {
		if (elements==null)
			return node;
		for(int i=0; i<elements.length; i++) {
			String name = elements[i];
			if (name.length()>0) {
				if (name.equals(".."))
					node=Node.getParent(node);
				else if (name.startsWith("@")) {
					// skip this is an attribute
				}
				else {
					String namespace = namespaces[i];
					if (namespace==null)
						node=NomUtil.getElementByLocalName(node, name);
					else
						node=NomUtil.getElement(node, namespace, name);
				}
			}
		}
		return node;
	}

}