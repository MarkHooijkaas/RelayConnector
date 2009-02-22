package org.kisst.cordys.http;

import org.kisst.cordys.relay.SoapFaultException;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.soap.BodyBlock;
import com.eibus.xml.nom.Node;

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
	private final HttpResponse response;

	
	public HttpSoapFaultException(HttpResponse response) {
		super("HTTP.Status."+response.getCode(), "HTTP call returned code "+response.getCode());
		this.response=response;
	}

	public void createResponse(BodyBlock responseBlock) {
		int cordysResponse=responseBlock.getXMLNode();
		int httpresponse=0;
		try {
			httpresponse = response.getResponseXml(Node.getDocument(cordysResponse));
			if (httpresponse !=0 && SoapUtil.isSoapFault(httpresponse))
				SoapUtil.mergeResponses(httpresponse, cordysResponse);
			else {
				cordysResponse=responseBlock.createSOAPFault(getFaultcode(),getFaultstring());
				Node.createCDataElement("details", response.getResponseString(), cordysResponse);
			}
		} 
		finally {
			if (httpresponse!=0)
				Node.delete(httpresponse);
		}
	}
}
