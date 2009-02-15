package org.kisst.cordys.http;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.util.NomUtil;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

public class HttpRelayStep extends HttpBaseStep implements Step {
	private final boolean wsa;
	
	public HttpRelayStep(CompilationContext compiler, final int node) {
		super(compiler, node);
		wsa = compiler.getSmartBooleanAttribute(node, "wsa", false);
	}
	
	public void executeStep(final ExecutionContext context) {
	    int bodyNode= body.getNode(context);
	    int httpResponse = 0;
	    try {
		    if (wsa) {
		    	bodyNode=Node.clone(bodyNode, true);
		    	wsaTransform(bodyNode);
		    }
	    	String xml=Node.writeToString(bodyNode, prettyPrint);
		    		
	    	byte[] responseBytes=call(context, xml);
	    	httpResponse = context.getDocument().load(responseBytes);
			int cordysResponse=context.getXmlVar("output");

			SoapUtil.mergeResponses(httpResponse, cordysResponse);
	    }
	    catch (XMLException e) { throw new RuntimeException(e); }
	    finally {
	    	Node.delete(httpResponse);
	    	if (wsa)
	    		Node.delete(bodyNode);
	    }
	}

	private void wsaTransform(int top) {
		int header=NomUtil.getElement(top, SoapUtil.SoapNamespace, "Header");
		//int to=NomUtil.getElement(header, wsaNamespace, "To");
		//if (to==0)
		//	throw new RuntimeException("Missing wsa:To element");
		int refpar=Node.createElement("ReferenceParameters", header);
		NomUtil.setNamespace(refpar, SoapUtil.wsaNamespace, "wsa", false);
		int cb=Node.createElement("HttpConnectorCallback", refpar);
		NomUtil.setNamespace(cb, "kisst.org", "kisst", false);
		moveNode(header, "ReferenceParameters", cb);
		moveNode(header, "ReplyTo", cb);
		moveNode(header, "FaultTo", cb);
	}

	private void moveNode(int header, String name, int dest) {
		int orig = NomUtil.getElement(header, SoapUtil.wsaNamespace, name);
		if (orig==0)
			return;
		Node.unlink(orig);
		Node.appendToChildren(orig,dest);
	}
}
