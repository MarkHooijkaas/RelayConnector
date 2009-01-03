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
	    try {
	    	int responseNode = context.getDocument().load(responseBytes);
			int output=context.getXmlVar("output");
			output=Node.getParent(output); // get Soap:Body
			output=Node.getParent(output); // get Soap:Envelope
			
			// clear entire boilerplate response: clear Header and Body children, remove attributes
			// Note, the nodes are left intact. It seems necessary not to delete these
			int child=Node.getFirstChild(output);
			int originalCordysHeader=0;
			while (child!=0) {
				// there should only be two children (Header and Body)
				if (Node.getLocalName(child).equals("Header")) {
					originalCordysHeader=Node.getElement(child, "header");
					originalCordysHeader=Node.unlink(originalCordysHeader);
				}
				NomUtil.clearNode(child);
				child=Node.getNextSibling(child);
			}
			//NomUtil.clearAttributes(output);

			
			// copy Envelope attributes
			//NomUtil.copyAttributes(responseNode, output);
			// copy children (Header and Body)
			int srcchild=Node.getFirstChild(responseNode);
			while (srcchild!=0) {  
				int destchild=Node.getElement(output, Node.getLocalName(srcchild));
				if (destchild==0) // Node did not exist (should not happen)
					destchild=Node.createElement(Node.getLocalName(responseNode), output);
				NomUtil.copyAttributes(srcchild, destchild);
				if (Node.getLocalName(srcchild).equals("Header")) {
					Node.appendToChildren(originalCordysHeader, destchild);
				}
				Node.duplicateAndAppendToChildren(Node.getFirstChild(srcchild), Node.getLastChild(srcchild), destchild );
				srcchild=Node.getNextSibling(srcchild);
			}
	    }
	    catch (XMLException e) { throw new RuntimeException(e); }
	}

}
