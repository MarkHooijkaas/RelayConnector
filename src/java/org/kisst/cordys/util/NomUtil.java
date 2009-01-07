package org.kisst.cordys.util;

import com.eibus.xml.nom.Node;

public class NomUtil {
	//public static final String SoapNamespace ="http://www.w3.org/2003/05/soap-envelope";
	public static final String SoapNamespace="http://schemas.xmlsoap.org/soap/envelope/";
	// public static final String SoapNamespace ="http://www.w3.org/2001/12/soap-envelope";
	public static boolean getBooleanAttribute(int node, String attrName , boolean defaultValue) {
    	String str = Node.getAttribute(node, attrName);
		if (str==null)
			return defaultValue;
		if (str.equals("true") )
    		return true;
    	if (str.equals("false") )
        	return false;
   		throw new RuntimeException("Optional attribute "+attrName+" must have value true or false, not "+str);
	}
	
	/**
	 * Returns the number of children elemens with given name
	 */
	public static int countElements(int node, String name) {
		int count=0;
		int child=Node.getFirstChild(node);
		while (child!=0) {
			if (Node.getLocalName(child).equals(name))
				count++;
			child=Node.getNextSibling(child);
		}
		return count;
	}
	
	public static String nodeToString(int node) {
		String s="<"+Node.getLocalName(node);
		for (int a=0; a<Node.getNumAttributes(node); a++) {
			String attrName=Node.getAttributeName(node, a);
			s+=" "+attrName+"=\""+Node.getAttribute(node, attrName)+"\"";
		}
		s+=">";
		return s;
	}

	/**
	 * Finds a element with a certain tagname, with a certain namespace  
	 * @param count if multiple elements with same name and namespace exist, this is an index (starting at 0)
	 * @return returns 0 if not found 
	 */
	public static int getElement(int node, String namespace, String tag) {
		return getElement(node, namespace, tag, 0);
	}
		/**
	 * Finds a element with a certain tagname, with a certain namespace  
	 * @param count if multiple elements with same name and namespace exist, this is an index (starting at 0)
	 * @return returns 0 if not found 
	 */
	public static int getElement(int node, String namespace, String tag, int index) {
		node=Node.getFirstChild(node);
		while (node!=0) {
			if (tag.equals(Node.getLocalName(node)) && namespace.equals(Node.getNamespaceURI(node))) {
				if (index==0)
					return node;
				index--;
			}
			node=Node.getNextSibling(node);
		}
		return 0;
	}
	
	/**
	 * returns the attribute of an element, or if not found, it looks upward to parent
	 * Can be used for searching xmlns attributes
	 * @return null if not found, otherwise the value of the attribute
	 */
	public static String getAttributeUpwards(int node, String attributeName) {
		while (node!=0) {
			String value=Node.getAttribute(node, attributeName);
			if (value!=null)
				return value;
			node=Node.getParent(node);
		}
		return null;
	}

	public static void setNamespace(int node, String namespace, String prefix, boolean reduceXmlns) {
		if (namespace==null)
			throw new RuntimeException("namespace is null when trying to define it for element named "+Node.getName(node));
		if (prefix==null) {
			// use default namespace mechanism
			// remove the prefix.
			Node.setName(node, Node.getLocalName(node));
			
			if (namespace.equals(Node.getNamespaceURI(node)) && reduceXmlns)
				// nothing needs to be done
				return;
				
			Node.setAttribute(node, "xmlns", namespace);
		}
		else {
			// Look if prefix is already known
			if ((!reduceXmlns) || !namespace.equals(getAttributeUpwards(node,"xmlns:"+prefix)))
				Node.setAttribute(node, "xmlns:"+prefix, namespace);
			Node.setName(node, prefix+":"+Node.getLocalName(node));
		}
	}

	public static void clearNode(int node) {
		Node.delete(Node.getFirstChild(node), Node.getLastChild(node));
		clearAttributes(node);
	}
	public static void clearAttributes(int node) {
		Node.setName(node, Node.getLocalName(node)); // without prefix, because attributes are deleted
		for (int i=Node.getNumAttributes(node); i>=0; i--)
			Node.removeAttribute(node, Node.getAttributeName(node, i));
	}
	
	public static void copyAttributes(int src, int dest) {
		int count=Node.getNumAttributes(src);
		for (int i=0; i<count; i++) {
			String name=Node.getAttributeName(src, i);
			Node.setAttribute(dest, name, Node.getAttribute(src, name));
		}
		Node.setName(dest, Node.getName(src));
	}

}
