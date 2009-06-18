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
