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

public class SoapUtil {
	//public static final String SoapNamespace ="http://www.w3.org/2003/05/soap-envelope";
	public static final String soapNamespace="http://schemas.xmlsoap.org/soap/envelope/";
	// public static final String SoapNamespace ="http://www.w3.org/2001/12/soap-envelope";

	public static final String wsaNamespace="http://www.w3.org/2005/08/addressing";
	//public static final String wsaAnonymous="http://www.w3.org/2005/08/addressing/anonymous";

	public static final String defaultWsaWrapperElementNamespace = "http://kisst.org/cordys/http";
	public static final String defaultWsaWrapperElementName = "CallbackWrapper";

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

	/** Returns the faultstring of the SOAP:Fault element, or null if XML is not a SOAP:Fault
	 *  This is a helper routine that is generally called on the Envelope node, but can
	 *  also be used on the Body node, or the Fault node directly.
	 *  
	 * @param node the NOM node of a SOAP Envelope, Body or Fault
	 * @return the faultstring, or null if it is not 
	 */
	public static String getSoapFaultMessage(int node) {
		int fault=SoapUtil.getSoapFault(node);
		if (fault==0)
			return null;
		return Node.getData(NomUtil.getElementByLocalName(fault,"faultstring"));
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

	/** Returns the NOM node of the SOAP:Header element
	 *  This is a helper routine that is called on the Envelope node
	 *  
	 * @param node the NOM node of a SOAP Envelope
	 * @return the NOM node of the SOAP Header element, or 0 if it is not 
	 */
	public static int getHeader(int envelope) {
		return NomUtil.getElement(envelope, soapNamespace, "Header");
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
		originalResponse = Node.getRoot(originalResponse); // get Soap:Envelope
		cordysResponse   = Node.getRoot(cordysResponse);   // get Soap:Envelope
		copyHeaders(originalResponse, cordysResponse);
		int srcbody=getBody(originalResponse);
		int destbody=getBody(cordysResponse);
		NomUtil.copyXmlnsAttributes(srcbody, destbody);
		NomUtil.deleteChildren(destbody); // remove boilerplate response 
		Node.duplicateAndAppendToChildren(Node.getFirstChild(srcbody), Node.getLastChild(srcbody), destbody );
	}

	/** 
	 * This function copies the SOAP:Header fields from one Envelope to another.
	 * The parameters do not need to point to the Envelope, but may point elsewhere
	 * because this function first does a getRoot on both parameters.  
	 */
	public static void copyHeaders(int src, int dest) {
		src=getHeader(Node.getRoot(src)); // get Soap:Header
		if (src==0)
			return; // no Header to copy
		int destheader=getHeader(Node.getRoot(dest)); // get Soap:Header
		if (destheader==0) {
			dest=Node.getRoot(dest);
			destheader=Node.createElement("Header", dest);
			NomUtil.setNamespace(destheader, soapNamespace, "SOAP", true);
		}
			
		// copy Envelope and Header attributes for xmlns definitions 
		// C2 will not do this for you 
		// In C3 the dupplicateAndAppend will do this for each child
		NomUtil.copyXmlnsAttributes(Node.getRoot(src), Node.getRoot(dest));
		NomUtil.copyXmlnsAttributes(src, dest);
		// copy children of Header
		int child=Node.getFirstChild(src);
		while (child!=0) {
			if (! isCordysHeader(child))
				Node.duplicateAndAppendToChildren(child, child, destheader );
			child=Node.getNextSibling(child);
		}
	}
	
	public static boolean isCordysHeader(int node) {
		if (! "header".equals(Node.getName(node)))
			return false;
		String ns=Node.getNamespaceURI(node);
		if (ns==null || ns.length()==0) // Cordys C2 did not use a namespace
			return true;
		return "http://schemas.cordys.com/General/1.0/".equals(ns);
	}
	
	public static void wsaTransformReplyTo(int top, String replyTo, String faultTo) {
		int header=NomUtil.getElement(top, SoapUtil.soapNamespace, "Header");
		moveNode(header, "ReplyTo", replyTo);
		moveNode(header, "FaultTo", faultTo);
	}

	private static void moveNode(int header, String name, String newAddress) {
		String wrappperElementName =defaultWsaWrapperElementName;
		String wrappperElementNamespace = defaultWsaWrapperElementNamespace;

		int node = NomUtil.getElement(header, SoapUtil.wsaNamespace, name);
		if (node==0) // TODO: FaultTo should maybe forced to anonymous
			return;
		int refpar=NomUtil.getElement(node, SoapUtil.wsaNamespace, "ReferenceParameters");
		if (refpar==0) {
			refpar=Node.createElement("ReferenceParameters", node);
			NomUtil.setNamespace(refpar, SoapUtil.wsaNamespace, "wsa", false);
		}
		int cb=Node.createElement(wrappperElementName, refpar); 
		NomUtil.setNamespace(cb, wrappperElementNamespace, "kisst", false);
		int origaddr = NomUtil.getElementByLocalName(node, "Address");
		String origaddress =Node.getData(origaddr); 
		int cbaddr=Node.createTextElement("Address", origaddress, cb);
		NomUtil.setNamespace(cbaddr, SoapUtil.wsaNamespace, "wsa", false);
		Node.setDataElement(origaddr, "", newAddress);
		
	}

}
