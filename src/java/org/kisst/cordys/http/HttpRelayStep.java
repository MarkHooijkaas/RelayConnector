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
		wrappperElementName     =compiler.getSmartAttribute(node, "wrapperName", HttpCallbackStep.defaultWrapperElementName);

		wrappperElementNamespace=compiler.getSmartAttribute(node, "wrapperNamespace", HttpCallbackStep.defaultWrapperElementNamespace);

		replyToExpression = ExpressionParser.parse(compiler, Node.getAttribute(node, "replyTo"));
		if (wsa && replyToExpression==null)
			throw new CompilationException(compiler, "when wsa attribute is true a replyTo attribute is mandatory");
		faultToExpression = ExpressionParser.parse(compiler, Node.getAttribute(node, "faultTo"));
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
			if (xmlResponse) {
				httpResponse = response.getResponseXml(context.getDocument());
				SoapUtil.mergeResponses(httpResponse, cordysResponse);
			}
		}
		finally {
			if (httpResponse!=0) Node.delete(httpResponse);
			if (bodyNode!=0) Node.delete(bodyNode);
		}
	}

	private void wsaTransform(final ExecutionContext context, int top) {
		int header=NomUtil.getElement(top, SoapUtil.soapNamespace, "Header");
		//int to=NomUtil.getElement(header, wsaNamespace, "To");
		//if (to==0)
		//      throw new RuntimeException("Missing wsa:To element");
		int refpar=NomUtil.getElement(header, SoapUtil.wsaNamespace, "ReferenceParameters");
		if (refpar==0) {
			refpar=Node.createElement("ReferenceParameters", header);
			NomUtil.setNamespace(refpar, SoapUtil.wsaNamespace, "wsa", false);
		}
		int cb=Node.createElement(wrappperElementName, refpar); // TODO: Check if this node already exists...
		NomUtil.setNamespace(cb, wrappperElementNamespace, "kisst", false);
		moveNode(header, "ReplyTo", cb);
		moveNode(header, "FaultTo", cb);
		int replyToNode=Node.createTextElement("ReplyTo", replyToExpression.getString(context), header);
		NomUtil.setNamespace(replyToNode, SoapUtil.wsaNamespace, "wsa", false);
		if (faultToExpression!=null) {
			int faultToNode=Node.createElement("FaultTo", header);
			NomUtil.setNamespace(faultToNode, SoapUtil.wsaNamespace, "wsa", false);
			Node.setData(faultToNode, faultToExpression.getString(context));
		}
	}

	private void moveNode(int header, String name, int dest) {
		int orig = NomUtil.getElement(header, SoapUtil.wsaNamespace, name);
		if (orig==0)
			return;
		Node.unlink(orig);
		Node.appendToChildren(orig,dest);
	}
}