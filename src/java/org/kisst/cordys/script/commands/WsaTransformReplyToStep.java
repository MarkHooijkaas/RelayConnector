package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.CompilationException;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class WsaTransformReplyToStep implements Step {
	private final XmlExpression xmlExpression;
	private final Expression replyToExpression;
	private final Expression faultToExpression;
	
	public WsaTransformReplyToStep(CompilationContext compiler, final int node) {
		xmlExpression = new XmlExpression(compiler, Node.getAttribute(node, "xml"));
		replyToExpression = ExpressionParser.parse(compiler, Node.getAttribute(node, "replyTo"));
		if (replyToExpression==null)
			throw new CompilationException(compiler, "when wsa attribute is true a replyTo attribute is mandatory");
		faultToExpression = ExpressionParser.parse(compiler, Node.getAttribute(node, "faultTo", "http://www.w3.org/2005/08/addressing/anonymous"));
	}

	public void executeStep(ExecutionContext context) {
		int node  = xmlExpression.getNode(context);
		String replyTo = replyToExpression.getString(context);
		String faultTo = faultToExpression.getString(context);

		SoapUtil.wsaTransformReplyTo(node, replyTo, faultTo);
		if (context.debugTraceEnabled()) {
			if (context.getCallContext().getTrace()!=null)
				context.getCallContext().getTrace().trace(Severity.DEBUG, "result after wsa transform ", node);
		}
	}
}
