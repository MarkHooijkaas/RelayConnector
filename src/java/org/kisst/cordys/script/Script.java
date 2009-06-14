package org.kisst.cordys.script;

import java.util.HashMap;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.relay.RelayConnector;
import org.kisst.cordys.relay.RelayTransaction;
import org.kisst.cordys.util.NomUtil;

import com.eibus.xml.nom.Node;

public class Script implements Step {
	private final CompilationContext compiler;
	private final Step[] steps;
	private final HashMap<String,String> prefixes;
	private final Props props;
	private final RelayTrace trace;
	private final RelayConnector relayConnector;
	
	public Props getProps() { return props; }
	public RelayTrace getTrace() { return trace; }
	public RelayConnector getRelayConnector() {	return relayConnector; }


	public Script(RelayTransaction trans, int scriptNode) {
		this.compiler = new CompilationContext(this);
		this.prefixes = new HashMap<String,String>();
		this.props = trans.getProps();
		this.trace = trans.getTrace();
		this.relayConnector = trans.getRelayConnector();
		this.steps = new Step[Node.getNumChildren(scriptNode)];
		compile(compiler, scriptNode);
	}

	public Script(Script topscript, int scriptNode) {
		this.compiler = topscript.compiler;
		this.prefixes = topscript.prefixes;
		this.props=topscript.props;
		this.trace=topscript.trace;
		this.relayConnector = topscript.relayConnector;
		this.steps = new Step[Node.getNumChildren(scriptNode)];
		compile(compiler, scriptNode);
	}
	
	protected void compile(CompilationContext compiler, int scriptNode) {
		int i=0;
    	int node = Node.getFirstChild(scriptNode);
    	while (node != 0) {
    		try {
    			if (compiler.debugTraceEnabled())
    				compiler.traceDebug("compiling "+NomUtil.nodeToString(node));
    			steps[i]=compiler.compile(node);
    		}
    		catch (Exception e) {
    			String s = NomUtil.nodeToString(node);
    			Throwable t=e;
    			while (t.getCause()!=null)
    				t=t.getCause();
    			throw new CompilationException(compiler, "Error when parsing "+s+", original error: "+t.getMessage(),e);
    		}
    		node = Node.getNextSibling(node);
    		i++;
    	}
	}

	public void executeStep(ExecutionContext context) {
		for (int i=0; i<steps.length; i++) {
			if (steps[i]!=null) {// skip null steps 
				if (context.debugTraceEnabled())
					context.traceDebug("executing "+steps[i].toString());
				steps[i].executeStep(context);
			}
		}
	}

	public void addPrefix(String prefix, String namespace) {
		if (prefixes.containsKey(prefix))
			throw new RuntimeException("prefix "+prefix+" allready defined when trying to set new namespace "+namespace);
		prefixes.put(prefix,namespace);
	}
	public String resolvePrefix(String prefix) {
		if (! prefixes.containsKey(prefix))
			throw new RuntimeException("unknown prefix "+prefix);
		return prefixes.get(prefix);
	}
}

