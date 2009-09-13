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
import org.kisst.cordys.script.xml.ElementAppender;

import com.eibus.xml.nom.Node;


public class OutputStep implements Step {
	private final ElementAppender appender;
	private final String rename;

	public OutputStep(CompilationContext compiler, int node) {
		appender=new ElementAppender(compiler, node);
		rename=Node.getAttribute(node, "rename");
	}

	public void executeStep(ExecutionContext context) {
		int node=context.getXmlVar("output");
		if (rename!=null) {
			if (context.debugTraceEnabled())
				context.traceDebug("renaming output node to "+rename);
			Node.setName(node, rename);
		}
		appender.append(context, node);
	}
}