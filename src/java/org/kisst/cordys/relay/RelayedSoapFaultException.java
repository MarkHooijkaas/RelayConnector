package org.kisst.cordys.relay;

import java.io.UnsupportedEncodingException;

import org.kisst.cordys.util.SoapUtil;

import com.eibus.soap.BodyBlock;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;


public class RelayedSoapFaultException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final String envelope;

	public RelayedSoapFaultException(String message, String envelope) {
		super(message);
		this.envelope=envelope;
	}
	public RelayedSoapFaultException(int envelope) {
		this(SoapUtil.getSoapFaultMessage(envelope), Node.writeToString(envelope, false));
	}
	public void createResponse(BodyBlock responseBlock) {
		int cordysResponse=responseBlock.getXMLNode();
		int response=0;
		try {
			response = Node.getDocument(cordysResponse).parseString(envelope);
			SoapUtil.mergeResponses(response, cordysResponse);
		}
		catch (XMLException e) { throw new RuntimeException(e); } 
		catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
		finally {
			if (response!=0)
				Node.delete(response);
		}
	}	
}	
