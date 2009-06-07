package org.kisst.cordys.script;

import java.util.Date;
import java.util.HashMap;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.relay.RelayConnector;

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
		int nodeToDestroy=0;
		private XmlVar(int node, int nodeToDestroy) { this.node=node; this.nodeToDestroy=nodeToDestroy;}
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
	private final Document doc;
	private RuntimeException asynchronousError=null;
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
			xmlvars.put(name, new XmlVar(node, nodeToDestroy));
			this.notifyAll(); // notify should suffice as well instead of notifyAll
		}
	}

	synchronized public void setTextVar(String name, String value) {
		if (allreadyDestroyed)
			logger.log(Severity.WARN, "Trying to set text var ["+name+"] on allready destroyed context to value ["+value+"]");
		textvars.put(name, new TextVar(value));
	}
	
	synchronized public String getTextVar(String name) {
		if (asynchronousError!=null)
			throw asynchronousError;
		return textvars.get(name).str;
	}

	synchronized public int getXmlVar(String name) {
		while (true) {
			if (allreadyDestroyed)
				throw new RuntimeException("Trying to get xml var ["+name+"] from allready destroyed context");
			if (asynchronousError!=null)
				throw asynchronousError;
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

	synchronized public void destroy() {
		if (allreadyDestroyed)
			throw new RuntimeException("Trying to destroy allready destroyed context");
		allreadyDestroyed=true;
		// remove output (and input) nodes, so these are not destroyed
		for(XmlVar v:xmlvars.values()) {
			if (v.nodeToDestroy!=0)
				Node.delete(v.nodeToDestroy);
		}
	}

	public Props getProps() { return script.getProps(); }
	public RelayConnector getRelayConnector() { return relayConnector; }
	public String getOrganization() { return organization; }	
	public String getOrganizationalUser() {	return user; }
	public Document getDocument() { return doc; }

	public synchronized void setAsynchronousError(RuntimeException asynchronousError) {
		this.asynchronousError = asynchronousError;
		this.notifyAll();
	} 
}
