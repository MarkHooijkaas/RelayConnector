package org.kisst.cordys.script;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

/**
 * This class supports tracing all details of method calls, for debugging purposes.
 * This could not be done using log4j only, because it is meant that tracing can be turned on and off for 
 * individual methods. 
 * Furthermore, the trace should be appended to any response or SOAP:Fault that the method returns.
 */
public class RelayTrace {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(RelayTrace.class);

	private StringBuffer trace;
	private Severity traceLevel=null;

	public void traceDebug(String msg) { 
		if (debugTraceEnabled())
			trace(Severity.DEBUG,msg);

	} 
	public void traceInfo(String msg) {	trace(Severity.INFO,msg); }
	public synchronized void trace(Severity level, String msg) { 
		logger.log(level,msg);
		if (! infoTraceEnabled()) 
			// trace should be at least on info level, to be added to the trace buffer
			// otherwise an ERROR would fill the trace 
			return;
		if (trace==null)
			trace=new StringBuffer();
		trace.append(msg+"\n");
	} 

	public void setTrace(Severity level) {	traceLevel=level; }
	public boolean debugTraceEnabled() { return (traceLevel!=null && traceLevel.isGreaterOrEqual(Severity.DEBUG)) || logger.isDebugEnabled(); }
	public boolean infoTraceEnabled()  { return (traceLevel!=null && traceLevel.isGreaterOrEqual(Severity.INFO )) || logger.isInfoEnabled(); }
	public String getTrace() {
		if (trace==null)
			return null;
		return trace.toString();
	}
	
	public static Severity parseSeverity(String sev) {
		if (sev==null)           return null;
		if (sev.equals("NONE"))  return null;
		if (sev.equals("DEBUG")) return Severity.DEBUG;
		if (sev.equals("INFO"))  return Severity.INFO;
		if (sev.equals("WARN"))  return Severity.WARN;
		if (sev.equals("ERROR")) return Severity.ERROR;
		if (sev.equals("FATAL")) return Severity.FATAL;
		throw new RuntimeException("unknown LogLevel ["+sev+"] should be NONE, DEBUG, INFO, WARN, ERROR or FATAL");
	}
	
}
