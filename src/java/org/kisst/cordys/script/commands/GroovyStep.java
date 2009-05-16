package org.kisst.cordys.script.commands;

import java.io.ByteArrayInputStream;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.util.NomNode;

import com.eibus.xml.nom.Node;

public class GroovyStep implements Step {
	private final static GroovyClassLoader loader = new GroovyClassLoader();
	private final Class scriptClass;
	
	
	public GroovyStep(CompilationContext compiler, final int node) {
		String scriptText = Node.getData(node);
		scriptClass = loader.parseClass(new ByteArrayInputStream(scriptText.toString().getBytes()), "dummySource");
	}

	public void executeStep(ExecutionContext context) {
		try {
			// Does each new invocation need a new instance?
			// probably, considering the bindings
			Script script = (Script) scriptClass.newInstance();
			script.getBinding().setVariable("context", context);
			script.getBinding().setVariable("input",  new NomNode(context.getXmlVar("input")));
			script.getBinding().setVariable("output", new NomNode(context.getXmlVar("output")));
			script.run();
		} 
		catch (InstantiationException e) { throw new RuntimeException(e); }
		catch (IllegalAccessException e) { throw new RuntimeException(e); }
	}
}
