package org.kisst.cordys.script;

import java.util.HashMap;

import org.kisst.cordys.relay.RelayConfiguration;
import org.kisst.cordys.relay.RelayConnector;

import com.eibus.soap.BodyBlock;
import com.eibus.soap.MethodDefinition;

public class TopScript extends Script {
	private final RelayConnector relayConnector;
	private final MethodDefinition definition;
	private final String name;
	private final HashMap<String,String> prefixes = new HashMap<String,String>();  

	public TopScript(RelayConnector connector, MethodDefinition def) {
		super(def.getImplementation());
		this.relayConnector=connector; 
		this.definition=def;
    	this.name=getFullMethodName();
		CompilationContext compiler=new CompilationContext(this); 
		compile(compiler, def.getImplementation());
	}

	public void execute(BodyBlock request, BodyBlock response) {
		ExecutionContext context=new ExecutionContext(relayConnector, request, response);
		try {
			executeStep(context);
		}
		finally {
			context.destroy();
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

	public String getName() { return name; }
	public String getMethodDn() {return definition.getMethodDN().toString(); }
	public String getMethodName() {	return definition.getMethodName();	}
	public String getMethodNamespace() { return definition.getNamespace();	}
	public String getFullMethodName() {	return getMethodNamespace()+"/"+getMethodName(); }
	public RelayConnector getRelayConnector() { return relayConnector; }
	public RelayConfiguration getConfiguration() { return relayConnector.conf; }

}
