package org.kisst.cordys.script;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.relay.MethodCache;
import org.kisst.cordys.relay.RelayConnector;
import org.kisst.cordys.relay.RelaySettings;
import org.kisst.cordys.script.commands.RelaySoapFaultException;
import org.kisst.cordys.util.Destroyable;
import org.kisst.cordys.util.NomNode;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.connector.nom.Connector;
import com.eibus.connector.nom.SOAPMessageListener;
import com.eibus.directory.soap.DirectoryException;
import com.eibus.soap.BodyBlock;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

public class ExecutionContext extends RelayTrace {
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
	private final TopScript script;
	private final RelayConnector  relayConnector;
	private final String organization;
	private final String user;
	private final HashMap<String,TextVar> textvars = new HashMap<String,TextVar>();
	private final HashMap<String,XmlVar>  xmlvars  = new HashMap<String,XmlVar>();
	private final ArrayList<Destroyable> destroyables = new ArrayList<Destroyable>();
	private final Document doc;
	private Exception asynchronousError=null;
	private boolean allreadyDestroyed=false;

	public ExecutionContext(TopScript script, RelayConnector connector, BodyBlock request, BodyBlock response) {
		this.script=script;
		this.relayConnector=connector; 
		this.organization=connector.getOrganization();

		user=request.getSOAPTransaction().getUserCredentials().getOrganizationalUser();
    	int inputNode = request.getXMLNode();
		int outputNode = response.getXMLNode();
		setXmlVar("input", inputNode, 0);
		setXmlVar("output", outputNode, 0);
		// This fixes some strange behavior that prefix of input is not used in output
		String inputPrefix= Node.getPrefix(inputNode);
		if (inputPrefix!=null)
			Node.setName(outputNode, inputPrefix+":"+Node.getLocalName(outputNode));
			
		doc=Node.getDocument(request.getXMLNode()); // TODO: is this the best document?
    }

	synchronized public void createXmlSlot(String name, String method, long timeoutTime) {
		xmlvars.put(name, new XmlVar(method, timeoutTime));
	}
	synchronized public void setXmlVar(String name, int node) {
		setXmlVar(name,node,node);
	}
	synchronized public void setXmlVar(String name, int node, int nodeToDestroy) {
		if (allreadyDestroyed) {
			logger.log(Severity.WARN, "Trying to set xml var ["+name+"] on allready destroyed context, deleting NOM node");
			if (nodeToDestroy!=0)
				Node.delete(nodeToDestroy);
		}
		else {
			if (nodeToDestroy!=0)
				destroyables.add(new NomNode(nodeToDestroy));
			xmlvars.put(name, new XmlVar(node));
			this.notifyAll(); // notify should suffice as well instead of notifyAll
		}
	}

	synchronized public void setTextVar(String name, String value) {
		if (allreadyDestroyed)
			logger.log(Severity.WARN, "Trying to set text var ["+name+"] on allready destroyed context to value ["+value+"]");
		textvars.put(name, new TextVar(value));
	}

	private void checkForException() {
		if (asynchronousError!=null) {
			if (asynchronousError instanceof RuntimeException)
				throw (RuntimeException) asynchronousError;
			else throw new RuntimeException(asynchronousError);
		}
	}
	
	synchronized public String getTextVar(String name) {
		checkForException(); 
		return textvars.get(name).str;
	}

	synchronized public int getXmlVar(String name) {
		while (true) {
			if (allreadyDestroyed)
				throw new RuntimeException("Trying to get xml var ["+name+"] from allready destroyed context");
			checkForException(); 
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

	/**
	 * Register object to be destroyed automatically when the call is done 
	 * @param destroyable the object to be destroyed
	 */
	public void destroyWhenDone(Destroyable destroyable) { 
		if (allreadyDestroyed) {
			logger.log(Severity.WARN, "Trying to register destroyable["+destroyable+"] on allready destroyed context, destroying it now");
			destroyable.destroy();
		}
		destroyables.add(destroyable); 
	}
	synchronized public void destroy() {
		if (allreadyDestroyed)
			throw new RuntimeException("Trying to destroy allready destroyed context");
		allreadyDestroyed=true;
		for(Destroyable d:destroyables)
			d.destroy();
	}

	public Props getProps() { return script.getProps(); }
	public RelayConnector getRelayConnector() { return relayConnector; }
	public String getOrganization() { return organization; }	
	public String getOrganizationalUser() {	return user; }
	public Document getDocument() { return doc; }

	public synchronized void setAsynchronousError(Exception e) {
		this.asynchronousError = e;
		this.notifyAll();
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
	
	public void callMethod(int method, String resultVar) {
		if (infoTraceEnabled())
			traceInfo("sending request\n"+Node.writeToString(method, true));
		MethodCache caller = getRelayConnector().responseCache;
		handleResponse(caller.sendAndWait(method,RelaySettings.timeout.get(getProps())), resultVar);
	}
	
	// TODO: fix possible memory leak if async calls never return a answer
	// Such call will register a SOAPListener which will never be removed.
	// In long term this could lead to a OutOfMemory error.
	// Possible fix would be to not use sendAndCallback, but just send, and use a 
	// default SOAPListener. This in considered not very urgent yet.

	public void callMethodAsync(int method, final String resultVar) {
		if (infoTraceEnabled())
			traceInfo("sending request\n"+Node.writeToString(method, true));
		MethodCache caller = getRelayConnector().responseCache;
		createXmlSlot(resultVar, "TODO", new Date().getTime()+RelaySettings.timeout.get(getProps()));
		caller.sendAndCallback(Node.getParent(method),new SOAPMessageListener() {
			public boolean onReceive(int message)
			{
				try {
					handleResponse(message, resultVar);
				}
				catch(Exception e) {
					setAsynchronousError(e);
				}
				return false; // Node should not yet be destroyed by Callback caller!!
			}
		});
	}
	private void handleResponse(int response, String resultVar) {
		destroyWhenDone(new NomNode(response));
		if (infoTraceEnabled()) // TODO: this might fail if context is already done and response is deleted
			traceInfo("received response\n"+Node.writeToString(response, true));
		if (allreadyDestroyed)
			return;
		int responseBody=SoapUtil.getContent(response);
		if (SoapUtil.isSoapFault(responseBody)) {
			trace(Severity.WARN, "Result of methodcall for "+resultVar+" returned Fault: "+Node.writeToString(responseBody, true));
			throw new RelaySoapFaultException(responseBody);
		}
		setXmlVar(resultVar, responseBody, 0);
	}

}
