package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.xml.ElementAppender;


public class OutputStep implements Step {
	private final ElementAppender appender;

	public OutputStep(CompilationContext compiler, int node) {
		appender=new ElementAppender(compiler, node);
	}

	public void executeStep(ExecutionContext context) {
		int node=context.getXmlVar("output");
		appender.append(context, node);
	}
}