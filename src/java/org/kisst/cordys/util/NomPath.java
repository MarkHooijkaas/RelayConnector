package org.kisst.cordys.util;

import com.eibus.xml.nom.Node;

public class NomPath {
	private final String[] elements;
	
	public NomPath(String str) {
		while (str.endsWith("/"))
			str=str.substring(0, str.length()-1);
		while (str.startsWith("/"))
			str=str.substring(1);

		if (str.trim().length()==0)
			elements=null;
		else
			elements=str.split("/");
	}
	
	public int findNode(int node) {
		if (elements==null)
			return node;
		for(String str:elements) {
			if (str.length()>0)
				node=Node.getElement(node, str);
		}
		return node;
	}
}
