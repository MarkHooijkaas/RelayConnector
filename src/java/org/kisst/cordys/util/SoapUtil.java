package org.kisst.cordys.util;

import com.eibus.xml.nom.Node;

public class SoapUtil {
	//public static final String SoapNamespace ="http://www.w3.org/2003/05/soap-envelope";
	public static final String SoapNamespace="http://schemas.xmlsoap.org/soap/envelope/";
	// public static final String SoapNamespace ="http://www.w3.org/2001/12/soap-envelope";

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

}
