package org.kisst.cordys.relay;

import org.kisst.cordys.util.NomUtil;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.xml.nom.Node;

public class SoapFaultException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public final String code;
	public final String message;

	public SoapFaultException(String code, String message) {
		super(message);
		this.code=code;
		this.message=message;
	}

	public SoapFaultException(String code, String message, Throwable e) {
		super(message, e);
		this.code=code;
		this.message=message;
	}
	
	public SoapFaultException(int node) {
		if ("Envelope".equals(Node.getLocalName(node)));
			node=NomUtil.getElement(node, SoapUtil.SoapNamespace, "Body");
		node=Node.getFirstChild(node);  // get response node
		// TODO: build in extra safety, when one passes in the Envelope or Body
		int codeNode=NomUtil.getElementByLocalName(node, "faultcode");
		code=Node.getData(codeNode);
		int messageNode=NomUtil.getElementByLocalName(node, "faultstring");
		message=Node.getData(messageNode);
	}
}
