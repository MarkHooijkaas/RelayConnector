package org.kisst.cordys.script;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

public class RelayTrace {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(RelayTrace.class);

	private StringBuffer trace;
	private boolean debugTrace;
	private boolean infoTrace;

	public synchronized void traceDebug(String msg) { 
		if (! debugTraceEnabled())
			return;
		if (trace==null)
			trace=new StringBuffer();
		trace.append(msg+"\n");
		logger.debug(msg);
	} 
	public synchronized void traceInfo(String msg) { 
		if (! infoTraceEnabled())
			return;
		if (trace==null)
			trace=new StringBuffer();
		trace.append(msg+"\n");
		logger.log(Severity.INFO,msg);
	} 
	public void setInfoTrace(boolean val) {	infoTrace=val;	}
	public void setDebugTrace(boolean val) {	debugTrace=val;	}
	public boolean debugTraceEnabled() { return debugTrace || logger.isDebugEnabled(); }
	public boolean infoTraceEnabled() { return infoTrace || debugTrace || logger.isInfoEnabled(); }
	public String getTrace() {
		if (trace==null)
			return null;
		return trace.toString();
	}
}
