package org.kisst.cordys.util;

import com.eibus.xml.nom.Node;

public class NomUtil {

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

}
