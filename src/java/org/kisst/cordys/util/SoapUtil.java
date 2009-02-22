package org.kisst.cordys.util;

import com.eibus.xml.nom.Node;

public class SoapUtil {
	//public static final String SoapNamespace ="http://www.w3.org/2003/05/soap-envelope";
	public static final String soapNamespace="http://schemas.xmlsoap.org/soap/envelope/";
	// public static final String SoapNamespace ="http://www.w3.org/2001/12/soap-envelope";

	public static final String wsaNamespace="http://www.w3.org/2005/08/addressing";
	//public static final String wsaAnonymous="http://www.w3.org/2005/08/addressing/anonymous";
	
	/** Returns true is the NOM node is a SOAP:Fault element
	 *  This is a helper routine that is generally called on the Envelope node, but can
	 *  also be used on the Body node, or the Fault node directly 
	 *  
	 * @param node the NOM node of a SOAP Envelope, Body or Fault
	 * @return the NOM node of the SOAP Fault element, or 0 if it is not 
	 */
	public static boolean isSoapFault(int node) {
		return getSoapFault(node)!=0;
	}

	/** Returns the NOM node of the SOAP:Fault element, or 0 if XML is not a SOAP:Fault
	 *  This is a helper routine that is generally called on the Envelope node, but can
	 *  also be used on the Body node, or the Fault node directly (in which case it does nothing).
	 *  
	 * @param node the NOM node of a SOAP Envelope, Body or Fault
	 * @return the NOM node of the SOAP Fault element, or 0 if it is not 
	 */
	public static int getSoapFault(int node) {
		if ("Envelope".equals(Node.getLocalName(node)))
			node=getBody(node);
		if ("Body".equals(Node.getLocalName(node)))
			node=NomUtil.getElement(node, soapNamespace, "Fault");
		if ("Fault".equals(Node.getLocalName(node)) && soapNamespace.equals(Node.getNamespaceURI(node)))
			return node;
		else 
			return 0;
	}
	
	/** Returns the NOM node of the SOAP:Body element
	 *  This is a helper routine that is called on the Envelope node
	 *  
	 * @param node the NOM node of a SOAP Envelope
	 * @return the NOM node of the SOAP Body element, or 0 if it is not 
	 */
	public static int getBody(int envelope) {
		return NomUtil.getElement(envelope, soapNamespace, "Body");
	}

	/** Returns the NOM node of the first child of the SOAP:Body element
	 *  This is a helper routine that is called on the Envelope node
	 *  
	 * @param node the NOM node of a SOAP Envelope
	 * @return the NOM node of the first child of the SOAP Body. 
	 */
	public static int getContent(int envelope) {
		int body=NomUtil.getElement(envelope, soapNamespace, "Body");
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
