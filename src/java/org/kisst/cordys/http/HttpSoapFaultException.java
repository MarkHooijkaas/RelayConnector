package org.kisst.cordys.http;

import org.kisst.cordys.relay.SoapFaultException;

import com.eibus.xml.nom.Node;

/**
 * This class is used when a HTTP call returns a HTTP code that indicates an error.
 * 
 * This class is passed the HTTP return code, and the response body of the call.
 * If the body is something else (e.g. some HTML message), a SOAP Fault is generated, that 
 * includes the response as SOAP Fault details.   
 *
 */
public class HttpSoapFaultException extends SoapFaultException {
	private static final long serialVersionUID = 1L;
	private final HttpResponse response;

	public HttpSoapFaultException(HttpResponse response) {
		super("TECHERR.HTTP.Status."+response.getCode(), "HTTP call returned code "+response.getCode());
		this.response=response;
	}

	@Override
	protected boolean hasDetails()     { return true; }
	@Override
	protected void    fillDetails(int node)    { 
		Node.createTextElement("http-response", response.getResponseString(), node);
	}
	
}
