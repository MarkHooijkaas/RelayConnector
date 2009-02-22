package org.kisst.cordys.script.commands;

import org.kisst.cordys.relay.SoapFaultException;
import org.kisst.cordys.util.NomUtil;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.soap.BodyBlock;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

/**
 * This Exception is used to relay a received SOAP fault.
 * 
 * At this moment it only relays the faultcode, faultString and details fields.
 * A total merge of the received response would be better, but it is tricky to 
 * hold on to the NOM node, because this will be deleted.
 *    
 *
 */
public class RelaySoapFaultException extends SoapFaultException {
	private static final long serialVersionUID = 1L;

	private final String fullMessage;

	public RelaySoapFaultException(int node) {
		super(getFaultCode(node), getFaultString(node));
		// The message is not stored in NOM format, to prevent memory leaks
		fullMessage = Node.writeToString(node, false);
	}
	
	private static String getFaultCode(int node) {
		// The namespace is ignored, because this differs between C2 an C3
		int fault=SoapUtil.getSoapFault(node);
		return Node.getData(NomUtil.getElementByLocalName(fault, "faultcode"));
	}

	private static String getFaultString(int node) {
		// The namespace is ignored, because this differs between C2 an C3
		int fault=SoapUtil.getSoapFault(node);
		return Node.getData(NomUtil.getElementByLocalName(fault, "faultstring"));
	}
	
	public void createResponse(BodyBlock responseBlock) {
		int cordysResponse=responseBlock.getXMLNode();
		int response=0;
		try {
			response = Node.getDocument(cordysResponse).load(fullMessage);
			SoapUtil.mergeResponses(response, cordysResponse);
		} catch (XMLException e) { throw new RuntimeException(e); }
		finally {
			if (response!=0)
				Node.delete(response);
		}
	}
}
