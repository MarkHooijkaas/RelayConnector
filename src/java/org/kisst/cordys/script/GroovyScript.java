package org.kisst.cordys.script;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import org.codehaus.groovy.control.CompilationFailedException;

public class GroovyScript {
	private final static GroovyClassLoader loader=new GroovyClassLoader();

	private String script="";
	private int exprCount=0;

	// It seems we don't need a surrounding class, and also do not need 
	// a "def" before each function
	
	public String addExpression(String expr) {
		exprCount++;
		String name="expression"+exprCount;
		script +="Object "+name+"() { return "+expr+";}\n";
		return name;
	}

	public void addScript(String script) {
		this.script += script;
	}

	public GroovyObject compile() {
		if (script.length()==0)
			return null;
		try {
			return (GroovyObject) loader.parseClass(script).newInstance();
		}
		catch (CompilationFailedException e)  { throw new RuntimeException(e);}
		catch (InstantiationException e)  { throw new RuntimeException(e);}
		catch (IllegalAccessException e)  { throw new RuntimeException(e);}
	}
}
