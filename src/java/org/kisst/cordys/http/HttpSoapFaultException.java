package org.kisst.cordys.http;

import org.kisst.cordys.relay.SoapFaultException;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.soap.BodyBlock;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

/**
 * This class is used when a HTTP call returns a HTTP code that indicates an error.
 * 
 * This class is passed the HTTP return code, and the response body of the call.
 * If the Body contains a legal SOAP Fault message, this SOAP Fault is merged with the normal 
 * response Cordys generates.
 * If the body is something else (e.g. some HTML message), a SOAP Fault is generated, that 
 * includes the response as SOAP Fault details.   
 *
 */
public class HttpSoapFaultException extends SoapFaultException {
	private static final long serialVersionUID = 1L;
	private final byte[] responseBytes;

	
	public HttpSoapFaultException(int httpCode, byte[] response) {
		super("HTTP.Status."+httpCode, "HTTP call returned code "+httpCode);
		this.responseBytes=response;
	}

	public void createResponse(BodyBlock responseBlock) {
		int cordysResponse=responseBlock.getXMLNode();
		int httpresponse=0;
		try {
			httpresponse = Node.getDocument(cordysResponse).load(responseBytes);
			if (httpresponse !=0 && SoapUtil.isSoapFault(httpresponse))
				SoapUtil.mergeResponses(httpresponse, cordysResponse);
			else {
				cordysResponse=responseBlock.createSOAPFault(getFaultcode(),getFaultstring());
				Node.createCDataElement("details", responseBytes.toString(), cordysResponse);
			}
		} catch (XMLException e) { throw new RuntimeException(e); }
		finally {
			if (httpresponse!=0)
				Node.delete(httpresponse);
		}
	}
}
