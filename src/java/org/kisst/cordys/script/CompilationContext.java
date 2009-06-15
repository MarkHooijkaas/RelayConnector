package org.kisst.cordys.script;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.relay.CallContext;
import org.kisst.cordys.relay.RelayConnector;
import org.kisst.cordys.script.commands.CommandList;

import com.eibus.xml.nom.Node;

public class CompilationContext implements PrefixContext {
	private final CallContext ctxt;
	private final CommandList commands;
	private final HashSet<String> txtvars=new HashSet<String>();
	private final HashSet<String> xmlvars=new HashSet<String>();
	private final Stack<String> parsePath = new Stack<String>();
	private final HashMap<String,String> defaultAttributes = new HashMap<String,String>();

	public CompilationContext(CallContext ctxt, Script script)  {
		this.ctxt=ctxt;
		this.commands=new CommandList();
		
	    declareXmlVar("input");
		declareXmlVar("output");
    }

	public CallContext getCallContext() { return ctxt; } 
	public Props getProps() { return ctxt.getProps(); }
	public RelayConnector getRelayConnector() { return ctxt.getRelayConnector(); }

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

	public int getSmartIntAttribute(int node, String name, int defaultValue) {
		String str= getSmartAttribute(node,name, null);
		if (str==null)
			return defaultValue;
		return Integer.parseInt(str);
	}

	public String resolvePrefix(String prefix) { return ctxt.resolvePrefix(prefix); }
	public void addPrefix(String prefix, String namespace) { ctxt.addPrefix(prefix, namespace);}
}
