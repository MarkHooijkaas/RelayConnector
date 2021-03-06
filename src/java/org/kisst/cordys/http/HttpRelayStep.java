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

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.CompilationException;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;
import org.kisst.cordys.util.NomUtil;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.xml.nom.Node;

public class HttpRelayStep extends HttpBase2 implements Step {
	private final boolean wsa;

	private final String wrappperElementName;
	private final String wrappperElementNamespace;
	private final Expression replyToExpression;
	private final Expression faultToExpression;

	public HttpRelayStep(CompilationContext compiler, final int node) {
		super(compiler, node);
		wsa = compiler.getSmartBooleanAttribute(node, "wsa", false);
		wrappperElementName     =compiler.getSmartAttribute(node, "wrapperName", SoapUtil.defaultWsaWrapperElementName);

		wrappperElementNamespace=compiler.getSmartAttribute(node, "wrapperNamespace", SoapUtil.defaultWsaWrapperElementNamespace);

		replyToExpression = ExpressionParser.parse(compiler, Node.getAttribute(node, "replyTo"));
		if (wsa && replyToExpression==null)
			throw new CompilationException(compiler, "when wsa attribute is true a replyTo attribute is mandatory");
		faultToExpression = ExpressionParser.parse(compiler, Node.getAttribute(node, "faultTo", "http://www.w3.org/2005/08/addressing/anonymous"));
	}

	public void executeStep(final ExecutionContext context) {
		int bodyNode= 0;
		int httpResponse = 0;
		try {
			bodyNode= createBody(context);
			if (wsa)
				wsaTransform(context, bodyNode);
			HttpResponse response=call(context, bodyNode);
			int cordysResponse=context.getXmlVar("output");
			httpResponse = response.getResponseXml(context.getDocument());
			if (response.getCode()>=300) {
				if (httpResponse==0 || ! SoapUtil.isSoapFault(httpResponse))
					throw new HttpSoapFaultException(response);
			}
			if (httpResponse!=0) // This should als merge SOAP:Faults
				SoapUtil.mergeResponses(httpResponse, cordysResponse);
			// TODO: What to do if there is no XML response???
			// Note: if there is no XML response, ideally Cordys should give no XML response as well
			// However this is not possible since Cordys needs a SOAP:Header for routing purposes.
		}
		finally {
			if (httpResponse!=0) Node.delete(httpResponse);
			if (bodyNode!=0) Node.delete(bodyNode);
		}
	}

	private void wsaTransform(final ExecutionContext context, int top) {
		int header=NomUtil.getElement(top, SoapUtil.soapNamespace, "Header");
		moveNode(header, "ReplyTo", replyToExpression.getString(context));
		moveNode(header, "FaultTo", faultToExpression.getString(context));
	}

	private void moveNode(int header, String name, String newAddress) {
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
