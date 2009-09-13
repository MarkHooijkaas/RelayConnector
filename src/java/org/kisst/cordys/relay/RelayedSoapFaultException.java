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
