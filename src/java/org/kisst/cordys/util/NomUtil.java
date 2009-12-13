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

import com.eibus.xml.nom.Node;

public class NomUtil {
	static public int getIntAttribute(int node, String name, int defaultValue) {
		String tmp=Node.getAttribute(node, name);
		if (tmp!=null)
			return Integer.parseInt(tmp);
		else 
			return defaultValue;
	}

	static public long getLongAttribute(int node, String name, long defaultValue) {
		String tmp=Node.getAttribute(node, name);
		if (tmp!=null)
			return Long.parseLong(tmp);
		else 
			return defaultValue;
	}

	static public String getStringAttribute(int node, String name, String defaultValue) {
		String tmp=Node.getAttribute(node, name);
		if (tmp==null)
			return defaultValue;
		else
			return tmp;
	}
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
		int child=Node.getFirstElement(node);
		while (child!=0) {
			String nodename=Node.getLocalName(child);
			if (nodename!=null && nodename.equals(name))
				count++;
			child=Node.getNextSibling(child);
		}
		return count;
	}
	
	public static String nodeToString(int node) {
		String s="<"+Node.getLocalName(node);
		for (int a=1; a<=Node.getNumAttributes(node); a++) {
			String attrName=Node.getAttributeName(node, a);
			s+=" "+attrName+"=\""+Node.getAttribute(node, attrName)+"\"";
		}
		s+=">";
		return s;
	}

	/**
	 * Finds a element with a certain tagname, with a certain namespace  
	 * @return returns 0 if not found 
	 */
	public static int getElement(int node, String namespace, String tag) {
		return getElement(node, namespace, tag, 0);
	}
	

	/**
	 * Finds a element with a certain tagname, with a certain namespace  
	 * @param index if multiple elements with same name and namespace exist, this is an index (starting at 0)
	 * @return returns 0 if not found 
	 */
	public static int getElement(int node, String namespace, String tag, int index) {
		node=Node.getFirstElement(node);
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
	 * Finds a element with a certain tagname, no matter what namespace or prefix it has  
	 * @return returns 0 if not found 
	 */
	public static int getElementByLocalName(int node, String tag) {
		return getElementByLocalName(node, tag, 0);
	}

	/**
	 * Finds the last element with a certain tagname, no matter what namespace or prefix it has  
	 * @return returns 0 if not found 
	 */
	public static int getLastElementByLocalName(int node, String tag) {
		return getElementByLocalNameReverse(node, tag, 0);
	}		
	
	/**
	 * Finds a element with a certain tagname, no matter what namespace or prefix it has  
	 * @param index if multiple elements with same name exist, this is an index (starting at 0)
	 * @return returns 0 if not found 
	 */
	public static int getElementByLocalName(int node, String tag, int index) {
		node=Node.getFirstElement(node);
		while (node!=0) {
			if (tag.equals(Node.getLocalName(node))) {
				if (index==0)
					return node;
				index--;
			}
			node=Node.getNextSibling(node);
		}
		return 0;
	}

	/**
	 * Finds a element with a certain tagname, no matter what namespace or prefix it has.
	 * The search starts at the end  
	 * @param index if multiple elements with same name exist, this is an index (starting at 0)
	 * @return returns 0 if not found 
	 */
	public static int getElementByLocalNameReverse(int node, String tag, int index) {
		node=Node.getLastChild(node);
		while (node!=0) {
			if (tag.equals(Node.getLocalName(node))) {
				if (index==0)
					return node;
				index--;
			}
			node=Node.getPreviousSibling(node);
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
		if (prefix==null) {
			if (namespace==null)
				throw new RuntimeException("namespace is null when trying to define it for element named "+Node.getName(node));
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
			String current =getAttributeUpwards(node,"xmlns:"+prefix);
			if (current==null && namespace==null)
				throw new RuntimeException("namespace is unknown when trying to set prefix "+prefix+" for node "+Node.getName(node));
			if ((!reduceXmlns) || !namespace.equals(current))
				Node.setAttribute(node, "xmlns:"+prefix, namespace);
			Node.setName(node, prefix+":"+Node.getLocalName(node));
		}
	}

	public static void deleteChildren(int node) {
		Node.delete(Node.getFirstChild(node), Node.getLastChild(node));
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
	}

	public static void copyXmlnsAttributes(int src, int dest) {
		int count=Node.getNumAttributes(src);
		for (int i=0; i<count; i++) {
			String name=Node.getAttributeName(src, i);
			if (name.equals("xmlns") || name.startsWith("xmlns:"))
				Node.setAttribute(dest, name, Node.getAttribute(src, name));
		}
	}

	/**
	 * returns the universal name, looking like {<namespace>}<name>
	 * See http://www.jclark.com/xml/xmlns.htm
	 * 
	 * @param node
	 * @return
	 */
	public static String getUniversalName(int node) {
		String name=Node.getLocalName(node);
		String namespace=Node.getNamespaceURI(node);
		return "{"+namespace+"}"+name;
	}

	public static int getRootNode(int node) {
		do {
			int parent=Node.getParent(node);
			if (parent==0)
				return node;
			node=parent;
		} while(true);
	}

}
