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

package org.kisst.cordys.script;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.connector.BaseSettings;
import org.kisst.cordys.connector.CallTrace;
import org.kisst.cordys.script.commands.CommandList;

import com.eibus.xml.nom.Node;

public class CompilationContext extends CallTrace implements PrefixContext {
	private final HashMap<String,String> prefixes=new HashMap<String,String>();
	private final CommandList commands;
	private final HashSet<String> txtvars=new HashSet<String>();
	private final HashSet<String> xmlvars=new HashSet<String>();
	private final Stack<String> parsePath = new Stack<String>();
	private final HashMap<String,String> defaultAttributes = new HashMap<String,String>();
	private final Props props;

	public CompilationContext(Script script, Props props)  {
		super(BaseSettings.trace.get(props));
		this.props=props;
		this.commands=new CommandList();
		
	    declareXmlVar("input");
		declareXmlVar("output");
    }

	public Props getProps() { return props; }

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
	public String resolvePrefix(String prefix, String defaultValue) {
		if (! prefixes.containsKey(prefix))
			return defaultValue;
		return prefixes.get(prefix);
	}
}
