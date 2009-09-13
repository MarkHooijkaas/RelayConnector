package org.kisst.cordys.as400;

import org.kisst.cordys.as400.pcml.PcmlProgram;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;

import com.eibus.xml.nom.Node;
import com.ibm.as400.access.ProgramCall;

public class ProgramStep implements Step {

	private final PcmlProgram program;
	private final String input;
	private final String output;
	
	public ProgramStep(CompilationContext context, final int programNode) {
    	program = new PcmlProgram(context, programNode);
    	input=Node.getAttribute(programNode,"input");
    	output=Node.getAttribute(programNode,"output");
	}

	public void executeStep(ExecutionContext context) {
		int requestNode = context.getXmlVar(input);
		ProgramCall call = program.prepareProgramCall(requestNode);
		As400Module.getConnection(context).execute(call);
		program.processCallResult(call.getParameterList(), context.getXmlVar(output));
	}

}
