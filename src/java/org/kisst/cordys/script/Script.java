package org.kisst.cordys.script;

import org.kisst.cordys.relay.RelayConnector;
import org.kisst.cordys.util.NomUtil;

import com.eibus.soap.BodyBlock;
import com.eibus.soap.MethodDefinition;
import com.eibus.xml.nom.Node;

public class Script implements Step {
	private final String name;
	private final Step[] steps;
	
	public Script(RelayConnector connector, MethodDefinition def) {
		this(new CompilationContext(connector, def), def.getImplementation());
	}
	
	public Script(CompilationContext context, int scriptNode) {
		steps = new Step[Node.getNumChildren(scriptNode)];
		int i=0;
    	int node = Node.getFirstChild(scriptNode);
    	while (node != 0) {
    		try {
    			steps[i]=context.compile(node);
    		}
    		catch (Exception e) {
    			String s = NomUtil.nodeToString(node);
    			Throwable t=e;
    			while (t.getCause()!=null)
    				t=t.getCause();
    			throw new RuntimeException("Error when parsing "+s+", original error: "+t.getMessage(),e);
    		}
    		node = Node.getNextSibling(node);
    		i++;
    	}
    	this.name=context.getFullMethodName();
	}

	public String getName() {
		return name;
	}

	public void execute(RelayConnector connector, BodyBlock request, BodyBlock response) {
		ExecutionContext context=new ExecutionContext(connector, request, response);
		try {
			executeStep(context);
		}
		finally {
			context.destroy();
		}
	}

	public void executeStep(ExecutionContext context) {
		for (int i=0; i<steps.length; i++)
			if (steps[i]!=null) // Allow null steps
				steps[i].executeStep(context);
	}
}
