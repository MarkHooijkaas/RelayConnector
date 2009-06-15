package org.kisst.cordys.relay;

import java.util.ArrayList;
import java.util.HashMap;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.util.Destroyable;
import org.kisst.cordys.util.NomNode;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.connector.nom.Connector;
import com.eibus.directory.soap.DirectoryException;
import com.eibus.soap.SOAPTransaction;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

public class CallContext  {
	private final HashMap<String,String> prefixes=new HashMap<String,String>();

	protected final ArrayList<Destroyable> destroyables = new ArrayList<Destroyable>();
	private Exception asynchronousError=null;
	protected boolean allreadyDestroyed=false;

	
	protected final RelayConnector relayConnector;
	protected final String fullMethodName;
	protected final Props props;
	protected final RelayTrace trace;
	protected final RelayTimer timer;
	private final SOAPTransaction soapTransaction;

	public RelayConnector getRelayConnector() {	return relayConnector; }
	public String getFullMethodName() {	return fullMethodName;	}
	public Props getProps() { return props;	}
	public RelayTrace getTrace() {return trace;}
	public RelayTimer getTimer() { return timer;}
	public String getOrganization() { return relayConnector.getOrganization(); }	
	public String getOrganizationalUser() {	return soapTransaction.getUserCredentials().getOrganizationalUser();}
	public Document getDocument() { return Node.getDocument(soapTransaction.getRequestEnvelope()); } // TODO: is this the best document?
	public boolean allreadyDestroyed() { return allreadyDestroyed; }

	public CallContext(RelayConnector connector, String fullMethodName, Props props, SOAPTransaction stTransaction) {
		this.relayConnector=connector;
		this.fullMethodName=fullMethodName;
		this.props=props;
		this.soapTransaction=stTransaction;

		if (RelaySettings.trace.get(props))
			this.trace=new RelayTrace(Severity.DEBUG);
		else
			this.trace=null;
		if (RelaySettings.timer.get(props))
			timer=new RelayTimer();
		else
			timer=null;
	}
	
	public void traceInfo(String msg)  { if (trace!=null) trace.traceInfo(msg);	}
	public void traceInfo(String msg, int node)  { if (trace!=null) trace.trace(Severity.INFO, new RelayTrace.Item(msg,node));	}
	public void traceDebug(String msg) { if (trace!=null) trace.traceDebug(msg);	}
	public boolean debugTraceEnabled() { return (trace!=null) && trace.debugTraceEnabled();	}
	public boolean infoTraceEnabled()  { return (trace!=null) && trace.infoTraceEnabled();	}
	
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

	/**
	 * Register object to be destroyed automatically when the call is done 
	 * @param destroyable the object to be destroyed
	 */
	public void destroyWhenDone(Destroyable destroyable) { 
		if (allreadyDestroyed) {
			//logger.log(Severity.WARN, "Trying to register destroyable["+destroyable+"] on allready destroyed context, destroying it now");
			destroyable.destroy();
		}
		destroyables.add(destroyable); 
	}
	public void destroyWhenDone(int node) { destroyWhenDone(new NomNode(node));	}

	synchronized public void destroy() {
		if (allreadyDestroyed)
			throw new RuntimeException("Trying to destroy allready destroyed context");
		allreadyDestroyed=true;
		for(Destroyable d:destroyables)
			d.destroy();
	}


	public synchronized void setAsynchronousError(Exception e) {
		this.asynchronousError = e;
		this.notifyAll();
	}

	public void checkForException() {
		if (asynchronousError!=null) {
			if (asynchronousError instanceof RuntimeException)
				throw (RuntimeException) asynchronousError;
			else throw new RuntimeException(asynchronousError);
		}
	}
	
	public NomNode createMethod(String namespace, String methodname) {
		String dnUser=getOrganizationalUser();
		String dnOrganization=getOrganization();
		Connector connector = getRelayConnector().getConnector();
		try {
			int method = connector.createSOAPMethod(dnUser, dnOrganization, namespace, methodname);
			if (method!=0) {
				destroyWhenDone(new NomNode(Node.getParent(Node.getParent(method))));
				return new NomNode(method);
			}
			else
				return null;

		} 
		catch (DirectoryException e) {  throw new RuntimeException("Error when handling method "+methodname,e); }
	}
	
	public int callMethod(int method, String resultVar) {
		traceInfo("sending request: ", method);
		MethodCache caller = getRelayConnector().responseCache;
		int response = caller.sendAndWait(method,RelaySettings.timeout.get(getProps()));
		checkAndLogResponse(response, resultVar);
		return response;
	}
	
	public void checkAndLogResponse(int response, String resultVar) {
		destroyWhenDone(new NomNode(response));
		int responseBody=SoapUtil.getContent(response);
		if (infoTraceEnabled()) 
			// TODO: this might fail if context is already done and response is deleted
			traceInfo("received response: ",responseBody);
		if (allreadyDestroyed)
			return;
		if (SoapUtil.isSoapFault(responseBody)) {
			if (trace!=null)
				trace.trace(Severity.WARN, new RelayTrace.Item("Result of methodcall for "+resultVar+" returned Fault: ",responseBody));
			throw new RelayedSoapFaultException(response);
		}
	}

	
}
