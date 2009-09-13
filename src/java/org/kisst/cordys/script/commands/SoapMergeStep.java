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
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class SoapMergeStep implements Step {
	private final XmlExpression srcExpression;
	private final XmlExpression destExpression;
	
	
	public SoapMergeStep(CompilationContext compiler, final int node) {
		srcExpression = new XmlExpression(compiler, Node.getAttribute(node, "src"));
		destExpression = new XmlExpression(compiler, Node.getAttribute(node, "dest"));
	}

	public void executeStep(ExecutionContext context) {
		int src  = srcExpression.getNode(context);
		int dest = destExpression.getNode(context);
		SoapUtil.mergeResponses(src,dest);
		if (context.debugTraceEnabled()) {
			if (context.getCallContext().getTrace()!=null)
				context.getCallContext().getTrace().trace(Severity.DEBUG, "result after soap merge ", dest);
		}
	}
}
