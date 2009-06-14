package org.kisst.cordys.relay;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.script.RelayTrace;

import com.eibus.util.logger.Severity;

public class CallContext  {
	protected final RelayConnector relayConnector;
	protected final String fullMethodName;
	protected final Props props;
	protected final RelayTrace trace;
	
	public RelayConnector getRelayConnector() {	return relayConnector; }
	public String getFullMethodName() {	return fullMethodName;	}
	public Props getProps() { return props;	}
	public RelayTrace getTrace() {return trace;}
	public String getOrganization() { return relayConnector.getOrganization(); }	

	protected CallContext(CallContext ctxt) {
		this.relayConnector=ctxt.relayConnector;
		this.fullMethodName=ctxt.fullMethodName;
		this.props=ctxt.props;
		this.trace=ctxt.trace;
	}

	protected CallContext(RelayConnector connector, String methodName, Props props) {
		this.relayConnector=connector;
		this.fullMethodName=methodName;
		this.props=props;
		if (RelaySettings.trace.get(props))
			this.trace=new RelayTrace(Severity.DEBUG);
		else
			this.trace=null;
	}
	
	public void traceInfo(String msg)  { if (trace!=null) trace.traceInfo(msg);	}
	public void traceDebug(String msg) { if (trace!=null) trace.traceDebug(msg);	}
	public boolean debugTraceEnabled() { return (trace!=null) && trace.debugTraceEnabled();	}
	public boolean infoTraceEnabled()  { return (trace==null) && trace.infoTraceEnabled();	}
}
