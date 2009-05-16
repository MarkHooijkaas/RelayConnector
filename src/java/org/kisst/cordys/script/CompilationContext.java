package org.kisst.cordys.script;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import org.kisst.cordys.relay.RelayConfiguration;
import org.kisst.cordys.relay.RelayConnector;
import org.kisst.cordys.script.commands.CommandList;

import com.eibus.soap.MethodDefinition;
import com.eibus.xml.nom.Node;

public class CompilationContext {
	private final RelayConnector relayConnector;
	private final MethodDefinition definition;
	private final CommandList commands;
	private final HashSet<String> txtvars=new HashSet<String>();
	private final HashSet<String> xmlvars=new HashSet<String>();
	private final Stack<String> parsePath = new Stack<String>();
	private final HashMap<String,String> defaultAttributes = new HashMap<String,String>();  
	private final HashMap<String,String> prefixes = new HashMap<String,String>();  

	public CompilationContext(RelayConnector connector, MethodDefinition def)
    {
		this.relayConnector=connector; 
		this.definition=def;
		this.commands=CommandList.getBasicCommands();
	    
	    declareXmlVar("input");
		declareXmlVar("output");
    }

	public Step compile(int node) {
		String stepType=Node.getLocalName(node);
		Command cmd=commands.getCommand(stepType);
		if (cmd==null)
			throw new RuntimeException("unknown command "+stepType);
		pushActivity("compiling "+stepType);
		Step result=cmd.compileStep(node,this);
		popActivity();
		return result;
	}
	
	public void addCommand(String name, Command type) {	commands.addCommand(name, type); }

	public void declareXmlVar(String name) {
		xmlvars.add(name); 
	}
	public boolean xmlVarExists(String name) { 
		return xmlvars.contains(name); 
	}
	
	public void declareTextVar(String name) { 
		txtvars.add(name); 
	}
	public boolean textVarExists(String name) { 
		return txtvars.contains(name); 
	}

	public String getMethodDn() {
		return definition.getMethodDN().toString();
	}
	public String getMethodName() {
		return definition.getMethodName();
	}
	public String getMethodNamespace() {
		return definition.getNamespace();
	}
	public String getFullMethodName() {
		return getMethodNamespace()+"/"+getMethodName();
	}
	
	public void pushActivity(String descr) {
		parsePath.push(descr);
	}
	public String popActivity() {
		return parsePath.pop();
	}
	
	public void setDefaultAttribute(String name, String value) {
		defaultAttributes.put(name, value);
	}

	public String getSmartAttribute(int node, String name, String defaultValue) {
		String value=Node.getAttribute(node, name);
		if (value==null)
			value=defaultAttributes.get(name);
		if (value==null)
			value=defaultValue;
		return value;
	}

	public boolean getSmartBooleanAttribute(int node, String name, boolean defaultValue) {
		String str= getSmartAttribute(node,name, null);
		if (str==null)
			return defaultValue;
		return Boolean.parseBoolean(str); // TODO: more strict checking
	}

	public RelayConnector getRelayConnector() { return relayConnector; }
	public RelayConfiguration getConfiguration() { return relayConnector.conf; }
	
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
