package org.kisst.cordys.util;

import com.eibus.xml.nom.Node;

public class SoapUtil {
	//public static final String SoapNamespace ="http://www.w3.org/2003/05/soap-envelope";
	public static final String SoapNamespace="http://schemas.xmlsoap.org/soap/envelope/";
	// public static final String SoapNamespace ="http://www.w3.org/2001/12/soap-envelope";

	public static final String wsaNamespace="http://www.w3.org/2005/08/addressing";
	//public static final String wsaAnonymous="http://www.w3.org/2005/08/addressing/anonymous";
	
	public static boolean isSoapFault(int node) {
		if ("Envelope".equals(Node.getLocalName(node)))
			node=getContent(node);
		return "Fault".equals(Node.getLocalName(node)) 
				&& SoapNamespace.equals(Node.getNamespaceURI(node)); 

	}
	public static int getBody(int envelope) {
		return NomUtil.getElement(envelope, SoapNamespace, "Body");
	}
	public static int getContent(int envelope) {
		int body=NomUtil.getElement(envelope, SoapNamespace, "Body");
		return Node.getFirstChild(body);  
	}

	/** This function merges the response from some kind of call with the boilerplate response that Cordys made. 
	 * The boilerplate response Body should be cleared, but the Header has some important Cordys info.
	 * The merging is a bit tricky, because of possible namespace prefix differences.
	 */
	public static void mergeResponses(int originalResponse, int cordysResponse) {
		cordysResponse=Node.getParent(cordysResponse); // get Soap:Body
		cordysResponse=Node.getParent(cordysResponse); // get Soap:Envelope
		
		// copy Envelope attributes
		NomUtil.copyAttributes(originalResponse, cordysResponse);
		// copy children (Header and Body)
		int srcchild=Node.getFirstChild(originalResponse);
		while (srcchild!=0) {
			// Find the equivalent node in the cordysResponse 
			int destchild=NomUtil.getElement(cordysResponse, Node.getNamespaceURI(srcchild), Node.getLocalName(srcchild));
			if (destchild==0) { 
				// Node did not exist (should not happen)
				destchild=Node.createElement(Node.getLocalName(originalResponse), cordysResponse);
			}
			if (Node.getLocalName(srcchild).equals("Body"))
				NomUtil.clearNode(destchild); // Body needs boilerplate response child removed
			NomUtil.copyAttributes(srcchild, destchild);
			Node.duplicateAndAppendToChildren(Node.getFirstChild(srcchild), Node.getLastChild(srcchild), destchild );
			srcchild=Node.getNextSibling(srcchild);
		}
	}
	

}
