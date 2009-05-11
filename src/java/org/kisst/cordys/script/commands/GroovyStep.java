package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;

import com.eibus.xml.nom.Node;

public class GroovyStep implements Step {
	
	public GroovyStep(CompilationContext compiler, final int node) {
		String script = Node.getData(node);
		compiler.groovy.addScript(script);
	}

	public void executeStep(ExecutionContext context) {
		// TODO: execute this part
	}
}
