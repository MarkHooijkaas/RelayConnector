package org.kisst.cordys.util;

import java.util.ArrayList;

import org.kisst.cordys.relay.RelayTrace;

import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
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

	public void destroy() {
		try {
			Node.delete(node);
			if (false)
				throw new XMLException();
		}
		catch(XMLException e) {
			RelayTrace.logger.log(Severity.ERROR, "Error when deleting node (probably double delete), ignoring this error ",e);
		}
	}

	public NomNode[] getChildren() {
		NomNode[] result = new NomNode[Node.getNumChildren(node)];
		int idx=0;
		int n=Node.getFirstChild(node);
		while (n!=0) {
			result[idx++]=new NomNode(n);
			n=Node.getNextSibling(n);
		}
		return result;
	}

	public Object getAt(String path) {
		ArrayList<Object> result=new ArrayList<Object>();
		NomPath p=new NomPath(null, path); // Note: no prefixes possible yet
		p.fillNodeList(node, 0, result, false);
		if (p.isAlwaysSingle())
			return result.get(0);
		else
			return result;
	}
}
