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
