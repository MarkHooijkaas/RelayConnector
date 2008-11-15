package org.kisst.cordys.script;

import java.util.Date;
import java.util.HashMap;

import org.kisst.cordys.relay.RelayConnector;

import com.eibus.soap.BodyBlock;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

public class ExecutionContext {
	private static class XmlVar {
		String method;
		long timeoutTime;
		int node=0;
		private XmlVar(int node) { this.node=node;}
		private XmlVar(String method, long timeoutTime) { this.method=method; this.timeoutTime=timeoutTime;}
	}
	private static class TextVar {
		String str=null;
		private TextVar(String str) { this.str=str;}
	}
	private final RelayConnector  relayConnector;
	private final String organization;
	private final String user;
	private final HashMap<String,TextVar> textvars = new HashMap<String,TextVar>();
	private final HashMap<String,XmlVar>  xmlvars  = new HashMap<String,XmlVar>();
	private final Document doc;
	private RuntimeException asynchronousError=null;

	public ExecutionContext(RelayConnector connector, BodyBlock request, BodyBlock response) {
		this.relayConnector=connector; 
		this.organization=connector.getOrganization();
    	user=request.getSOAPTransaction().getUserCredentials().getOrganizationalUser();
		setXmlVar("input", request.getXMLNode());
		setXmlVar("output", response.getXMLNode());
		doc=Node.getDocument(request.getXMLNode()); // TODO: is this the best document?
    }

	synchronized public void createXmlSlot(String name, String method, long timeoutTime) {
		xmlvars.put(name, new XmlVar(method, timeoutTime));
	}
	synchronized public void setXmlVar(String name, int node) {
		xmlvars.put(name, new XmlVar(node));
		this.notifyAll(); // notify should suffice as well instead of notifyAll
	}

	synchronized public void setTextVar(String name, String value) {
		textvars.put(name, new TextVar(value));
	}
	
	synchronized public String getTextVar(String name) {
		if (asynchronousError!=null)
			throw asynchronousError;
		return textvars.get(name).str;
	}

	synchronized public int getXmlVar(String name) {
		while (true) {
			if (asynchronousError!=null)
				throw asynchronousError;
			XmlVar xmlvar=xmlvars.get(name);
			if (xmlvar==null)
				throw new RuntimeException("Unknown xml variable "+name);
			if (xmlvar.node != 0)
				return xmlvar.node;
			long now=new Date().getTime();
			if (now>xmlvar.timeoutTime)
				throw new RuntimeException("Timeout waiting for xml variable "+name+" as result from calling method "+xmlvar.method);
			try {
				this.wait(xmlvar.timeoutTime - now); 
			} 
			catch (InterruptedException e) { throw new RuntimeException("(timeout) exception while waiting for result "+name); }
		}
	}

	synchronized public void destroy() {
		// remove output (and input) nodes, so these are not destroyed
		xmlvars.remove("input");
		xmlvars.remove("output");
		for(XmlVar v:xmlvars.values()) {
			if (v.node!=0)
				Node.delete(v.node);
		}
	}

	public RelayConnector getRelayConnector() { return relayConnector; }
	public String getOrganization() { return organization; }	
	public String getOrganizationalUser() {	return user; }
	public Document getDocument() { return doc; }

	public synchronized void setAsynchronousError(RuntimeException asynchronousError) {
		this.asynchronousError = asynchronousError;
		this.notifyAll();
	} 
}
