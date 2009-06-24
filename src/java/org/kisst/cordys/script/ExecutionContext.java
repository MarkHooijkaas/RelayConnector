package org.kisst.cordys.script;

import java.util.Date;
import java.util.HashMap;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.relay.CallContext;
import org.kisst.cordys.relay.MethodCache;
import org.kisst.cordys.relay.RelayConnector;
import org.kisst.cordys.relay.RelaySettings;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.connector.nom.SOAPMessageListener;
import com.eibus.soap.BodyBlock;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class ExecutionContext {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(ExecutionContext.class);
	
	private static class XmlVar {
		String method;
		long timeoutTime;
		int node=0;
		private XmlVar(int node) { this.node=node; }
		private XmlVar(String method, long timeoutTime) { this.method=method; this.timeoutTime=timeoutTime;}
	}
	private static class TextVar {
		String str=null;
		private TextVar(String str) { this.str=str;}
	}

	private final HashMap<String,TextVar> textvars = new HashMap<String,TextVar>();
	private final HashMap<String,XmlVar>  xmlvars  = new HashMap<String,XmlVar>();
	private final CallContext ctxt;
	protected final BodyBlock request;
	protected final BodyBlock response;

	public ExecutionContext(CallContext ctxt, BodyBlock request, BodyBlock response) {
		this.ctxt=ctxt;
		this.request=request;
		this.response=response;
		
    	int inputNode = request.getXMLNode();
		int outputNode = response.getXMLNode();
		setXmlVar("input", inputNode);
		setXmlVar("output", outputNode);
		// This fixes some strange behavior that prefix of input is not used in output
		String inputPrefix= Node.getPrefix(inputNode);
		if (inputPrefix!=null)
			Node.setName(outputNode, inputPrefix+":"+Node.getLocalName(outputNode));
    }

	public CallContext getCallContext() { return ctxt; }
	public BodyBlock   getRequest() { return request; }
	public BodyBlock   getResponse() { return response; }
	public Props getProps() { return ctxt.getProps(); }
	public RelayConnector getRelayConnector() { return ctxt.getRelayConnector(); }

	synchronized public void createXmlSlot(String name, String method, long timeoutTime) {
		xmlvars.put(name, new XmlVar(method, timeoutTime));
	}
	synchronized public void setXmlVar(String name, int node) {
		xmlvars.put(name, new XmlVar(node));
		this.notifyAll(); // notify should suffice as well instead of notifyAll
	}

	synchronized public void setTextVar(String name, String value) {
		if (ctxt.allreadyDestroyed())
			logger.log(Severity.WARN, "Trying to set text var ["+name+"] on allready destroyed context to value ["+value+"]");
		textvars.put(name, new TextVar(value));
	}

	synchronized public String getTextVar(String name) {
		ctxt.checkForException();
		TextVar v = textvars.get(name);
		if (v==null)
			throw new RuntimeException("Unknown TextVar "+name);
		return v.str;
	}

	synchronized public int getXmlVar(String name) {
		while (true) {
			if (ctxt.allreadyDestroyed())
				throw new RuntimeException("Trying to get xml var ["+name+"] from allready destroyed context");
			ctxt.checkForException(); 
			XmlVar xmlvar=xmlvars.get(name);
			if (xmlvar==null)
				throw new RuntimeException("Unknown xml variable "+name);
			if (xmlvar.node != 0)
				return xmlvar.node;
			long now=new Date().getTime();
			if (now>=xmlvar.timeoutTime)
				throw new RuntimeException("Timeout waiting for xml variable "+name+" as result from calling method "+xmlvar.method);
			try {
				this.wait(xmlvar.timeoutTime - now); 
			} 
			catch (InterruptedException e) { throw new RuntimeException("(timeout) exception while waiting for result "+name); }
		}
	}

	// TODO: fix possible memory leak if async calls never return a answer
	// Such call will register a SOAPListener which will never be removed.
	// In long term this could lead to a OutOfMemory error.
	// Possible fix would be to not use sendAndCallback, but just send, and use a 
	// default SOAPListener. This in considered not very urgent yet.

	
	@SuppressWarnings("deprecation")
	public void callMethodAsync(int method, final String resultVar) {
		ctxt.traceInfo("sending request: ",method);
		String methodName=Node.getLocalName(method);
		MethodCache caller = ctxt.getRelayConnector().responseCache;
		createXmlSlot(resultVar, methodName, new Date().getTime()+RelaySettings.timeout.get(ctxt.getProps()));
		caller.sendAndCallback(Node.getParent(method),new SOAPMessageListener() {
			public boolean onReceive(int message)
			{
				try {
					ctxt.checkAndLogResponse(message, resultVar);
					setXmlVar(resultVar, SoapUtil.getContent(message));
				}
				catch(Exception e) {
					ctxt.setAsynchronousError(e);
				}
				return false; // Node should not yet be destroyed by Callback caller!!
			}
		});
	}
	
	public void traceInfo(String msg)  { ctxt.traceInfo(msg);	}
	public void traceDebug(String msg) { ctxt.traceDebug(msg);	}
	public boolean debugTraceEnabled() { return ctxt.debugTraceEnabled();	}
	public boolean infoTraceEnabled()  { return ctxt.infoTraceEnabled();	}

}
