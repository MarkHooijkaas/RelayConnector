package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;

import com.eibus.xml.nom.Node;

public class XmlnsStep implements Step {
	
	public XmlnsStep(CompilationContext compiler, final int node) {
		String prefix=Node.getAttribute(node, "prefix");
		String namespace=Node.getAttribute(node, "namespace");
		if (prefix==null)
			throw new RuntimeException("in command <xmlns...> there should be an attribute with the name \"prefix\"");
		if (namespace==null)
			throw new RuntimeException("in command <xmlns prefix=\""+prefix+"\" ...> there should be an attribute with the name \"namespace\"");
		compiler.addPrefix(prefix, namespace);
	}

	public void executeStep(ExecutionContext context) {
		// Do nothing, this is a compile time step
	}
}
