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
