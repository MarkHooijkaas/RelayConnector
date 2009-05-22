package org.kisst.cordys.script;

public class Traceable {
	private StringBuffer trace;
	private boolean debug;

	public synchronized void trace(String msg) { 
		if (trace==null)
			trace=new StringBuffer();
		trace.append(msg+"\n"); 
	} 
	public void setDebug(boolean val) {	debug=val;	}
	public boolean debug() { return debug; }
	public String getTrace() {
		if (trace==null)
			return null;
		return trace.toString();
	}
}
