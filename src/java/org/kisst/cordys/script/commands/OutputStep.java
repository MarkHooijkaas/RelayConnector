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