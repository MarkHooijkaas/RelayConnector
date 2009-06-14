package org.kisst.cordys.http;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;

import com.eibus.xml.nom.Node;

public class HttpStep extends HttpBase2 implements Step {
    protected final String resultVar;

	public HttpStep(CompilationContext compiler, final int node) {
		super(compiler, node);
		resultVar = Node.getAttribute(node, "resultVar", "output");
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
