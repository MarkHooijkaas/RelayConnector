package org.kisst.cordys.http;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;

import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

public class HttpStep extends HttpBaseStep implements Step {
    protected final String resultVar;

	public HttpStep(CompilationContext compiler, final int node) {
		super(compiler, node);
		resultVar = Node.getAttribute(node, "resultVar", "output");
		compiler.declareXmlVar(resultVar);
	}
	
	public void executeStep(final ExecutionContext context) {
		int bodyNode= body.getNode(context);
	    String xml=Node.writeToString(bodyNode, prettyPrint);
	    byte[] responseBytes=call(context, xml);
	    try {
	    	int responseNode = context.getDocument().load(responseBytes);
			context.setXmlVar(resultVar, responseNode);
	    }
	    catch (XMLException e) { throw new RuntimeException(e); }
	}
}
