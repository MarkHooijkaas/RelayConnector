package org.kisst.cordys.as400;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;

import com.eibus.xml.nom.Node;

public class CommandStep implements Step {

	private final String command;
	
	public CommandStep(CompilationContext compiler, final int node) {
		command=Node.getAttribute(node,"command");
	}

	public void executeStep(ExecutionContext context) {
		As400Module.getConnection(context).executeCommand(command);
	}

}
