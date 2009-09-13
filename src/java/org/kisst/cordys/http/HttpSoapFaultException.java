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
