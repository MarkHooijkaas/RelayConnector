package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;

import com.eibus.xml.nom.Node;

public class DefineDefaultStep implements Step {
	
	public DefineDefaultStep(CompilationContext compiler, final int node) {
		String attr=Node.getAttribute(node, "attribute");
		String value=Node.getAttribute(node, "value");
		if (attr==null)
			throw new RuntimeException("in command <default ...> there should be an attribute with the name \"attribute\"");
		if (value==null)
			throw new RuntimeException("in command <default attribute=\""+attr+"\" ...> there should be an attribute with the name \"attribute\"");
		compiler.setDefaultAttribute(attr, value);
	}

	public void executeStep(ExecutionContext context) {
		// Do nothing, this is a compile time step
	}
}
