package org.kisst.cordys.relay;

import java.util.ArrayList;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

/**
 * This class supports tracing all details of method calls, for debugging purposes.
 * This could not be done using log4j only, because it is meant that tracing can be turned on and off for 
 * individual methods. 
 * Furthermore, the trace should be appended to any response or SOAP:Fault that the method returns.
 */
public class RelayTrace {
	public static final CordysLogger logger = CordysLogger.getCordysLogger(RelayTrace.class);

	private final ArrayList<String> items=new ArrayList<String>();
	private final Severity traceLevel;

	public RelayTrace(Severity traceLevel) {
		this.traceLevel=traceLevel;
	}
	
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
		items.add(msg);
	} 

	public boolean debugTraceEnabled() { return (traceLevel!=null && Severity.DEBUG.isGreaterOrEqual(traceLevel)) || logger.isDebugEnabled(); }
	public boolean infoTraceEnabled()  { return (traceLevel!=null && Severity.INFO. isGreaterOrEqual(traceLevel)) || logger.isInfoEnabled(); }
	public String getTrace() {
		StringBuffer buf=new StringBuffer();
		for (String s:items)
			buf.append(s).append('\n');
		return buf.toString();
	}

	public void addToNode(int node) {
		for (String s:items)
			Node.createTextElement("item", s, node);
	}
}
