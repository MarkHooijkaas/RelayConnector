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
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;

import com.eibus.xml.nom.Node;

public class HttpStep extends HttpBase2 implements Step {
    private final String resultVar;
    private final boolean ignoreHttpErrorCode;
    private final boolean xmlResponse;

	public HttpStep(CompilationContext compiler, final int node) {
		super(compiler, node);
		resultVar = Node.getAttribute(node, "resultVar", "output");
		ignoreHttpErrorCode=HttpSettings.ignoreReturnCode.get(props);
		xmlResponse= compiler.getSmartBooleanAttribute(node, "xmlResponse", true);
		if (xmlResponse)
			compiler.declareXmlVar(resultVar);
		else
			compiler.declareTextVar(resultVar);
	}
	
	public void executeStep(final ExecutionContext context) {
		int bodyNode= 0;
		try {
			bodyNode= createBody(context);
		    HttpResponse response=call(context, bodyNode);
		    if (response.getCode()>=300 && ! ignoreHttpErrorCode)
		    	throw new HttpSoapFaultException(response);
			if (xmlResponse)
			    context.setXmlVar(resultVar, response.getResponseXml(context.getCallContext().getDocument()));
			else
				context.setTextVar(resultVar, response.getResponseString());
		}
		finally {
			if (bodyNode!=0) Node.delete(bodyNode);
		}
	}
}
