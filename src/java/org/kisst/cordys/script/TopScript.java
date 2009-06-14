package org.kisst.cordys.script;

import java.util.HashMap;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.relay.RelayConnector;

public class TopScript extends Script {
	private final RelayConnector relayConnector;
	private final HashMap<String,String> prefixes = new HashMap<String,String>();
	private final CompilationContext compiler;
	private final Props props;
	
	public TopScript(RelayConnector connector, int node, Props props) {
		super(node);
		this.props=props;
		this.relayConnector=connector; 
		compiler=new CompilationContext(this);
    	try {
    		compile(compiler, node);
    	}
    	catch (CompilationException e) { throw e; }
    	catch (Exception e) { throw new CompilationException(compiler, e.getMessage(), e); }
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

	public RelayConnector getRelayConnector() { return relayConnector; }
	public Props getProps() { return props; }
}
