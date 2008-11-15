package org.kisst.cordys.script;


public interface Command {
	public Step compileStep(int node, CompilationContext script);

}
