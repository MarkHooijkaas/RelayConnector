package org.kisst.cordys.http;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.util.NomUtil;

import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

public class HttpRelayStep extends HttpBaseStep implements Step {

	public HttpRelayStep(CompilationContext compiler, final int node) {
		super(compiler, node);
	}
	
	public void executeStep(final ExecutionContext context) {
		int bodyNode= body.getNode(context);
	    String xml=Node.writeToString(bodyNode, prettyPrint);
	    byte[] responseBytes=call(context, xml);
	    int httpResponse = 0;
	    try {
	    	// This code merges the response from the HTTP call with the
	    	// boilerplate response that Cordys made. The boilerplate response
	    	// Body should be cleared, but the Header has some important Cordys info.
	    	// The merging is a bit tricky, because of namespace prefix differences.
	    	httpResponse = context.getDocument().load(responseBytes);
			int cordysResponse=context.getXmlVar("output");
			cordysResponse=Node.getParent(cordysResponse); // get Soap:Body
			cordysResponse=Node.getParent(cordysResponse); // get Soap:Envelope
			
			// copy Envelope attributes
			NomUtil.copyAttributes(httpResponse, cordysResponse);
			// copy children (Header and Body)
			int srcchild=Node.getFirstChild(httpResponse);
			while (srcchild!=0) {
				// Find the equivalent node in the cordysResponse 
				int destchild=NomUtil.getElement(cordysResponse, Node.getNamespaceURI(srcchild), Node.getLocalName(srcchild));
				if (destchild==0) { 
					// Node did not exist (should not happen)
					destchild=Node.createElement(Node.getLocalName(httpResponse), cordysResponse);
				}
				if (Node.getLocalName(srcchild).equals("Body"))
					NomUtil.clearNode(destchild); // Body needs boilerplate response child removed
				NomUtil.copyAttributes(srcchild, destchild);
				Node.duplicateAndAppendToChildren(Node.getFirstChild(srcchild), Node.getLastChild(srcchild), destchild );
				srcchild=Node.getNextSibling(srcchild);
			}
	    }
	    catch (XMLException e) { throw new RuntimeException(e); }
	    finally {
	    	Node.delete(httpResponse);
	    }
	}

}
