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

import org.kisst.cordys.connector.CallTrace;

import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.NodeType;
import com.eibus.xml.nom.XMLException;

public class NomNode implements Destroyable {
	public final int node;
	public NomNode(int node) {this.node=node;}
	
	public String getLocalName() { return Node.getLocalName(node); } 
	public String getName() { return Node.getName(node); } 
	public String getFullName() { return NomUtil.getUniversalName(node); }
	public String getText() { return Node.getData(node); }
	
	public NomNode appendElement(String name) {	return new NomNode(Node.createElement(name, node));	}
	public void setText(String txt) {	Node.setDataElement(node, "", txt);	}
	public void appendText(String txt) {	Node.setDataElement(node, "", getText()+txt);	}
	public boolean containsOnlyText() {
		int child=Node.getFirstChild(node);
		while (child!=0) {
			if (Node.getType(child)!=NodeType.DATA && Node.getType(child)!=NodeType.CDATA)
				return false;
			child=Node.getNextSibling(child);
		}
		return true;
	}

	public void rename(String name) {
		String oldname=Node.getName(node);
		int pos=oldname.indexOf(':');
		if (pos>=0)
			// preserve prefix
			Node.setName(node, oldname.substring(0,pos+1)+name); 
		else
			Node.setName(node, name); 
	}
	public void setNamespace(String namespace, String prefix) { 
		NomUtil.setNamespace(node, namespace, prefix, true);
	}

	public void destroy() {
		boolean never=false;
		try {
			Node.delete(Node.getRoot(node));
			if (never) // Trick because XMLException is not in throws clause of native implementation
				throw new XMLException();
		}
		catch(XMLException e) {
			CallTrace.logger.log(Severity.ERROR, "Error when deleting node (probably double delete), ignoring this error ",e);
		}
	}

	public NomNode[] getChildren() {
		NomNode[] result = new NomNode[Node.getNumChildren(node)];
		int idx=0;
		int n=Node.getFirstElement(node);
		while (n!=0) {
			result[idx++]=new NomNode(n);
			n=Node.getNextSibling(n);
		}
		return result;
	}

	public Object get(String path) {
		NomPath p=new NomPath(null, path); // Note: no prefixes possible yet
		if (p.singleResult()) {
			if (p.stringResult())
				return p.getTextList(node).get(0);
			else
				return p.getNodeList(node).get(0);
		}
		else {
			if (p.stringResult())
				return p.getTextList(node);
			else
				return p.getNodeList(node);
		}
	}

}