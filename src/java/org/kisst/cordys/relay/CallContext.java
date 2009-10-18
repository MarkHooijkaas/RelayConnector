/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.kisst.cordys.relay;

import java.util.ArrayList;
import java.util.HashMap;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.relay.resourcepool.ResourcePool;
import org.kisst.cordys.util.Destroyable;
import org.kisst.cordys.util.NomNode;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.connector.nom.Connector;
import com.eibus.directory.soap.DirectoryException;
import com.eibus.soap.SOAPTransaction;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

public class CallContext extends RelayTrace {
	protected final ArrayList<Destroyable> destroyables = new ArrayList<Destroyable>();
	private Exception asynchronousError=null;
	protected boolean allreadyDestroyed=false;

	
	protected final RelayConnector relayConnector;
	protected final String fullMethodName;
	protected final Props props;
	protected final RelayTimer timer;
	private final SOAPTransaction soapTransaction;
	private ResourcePool resourcepool=null;
	
	private final HashMap<String,Object> objects=new HashMap<String,Object>();

	public RelayConnector getRelayConnector() {	return relayConnector; }
	public String getFullMethodName() {	return fullMethodName;	}
	public Props getProps() { return props;	}
	public RelayTrace getTrace() {return this;} // TODO: remove
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

		if (RelaySettings.timer.get(props))
			timer=new RelayTimer();
		else
			timer=null;
	}
	
	/**
	 * Register object to be destroyed automatically when the call is done 
	 * @param destroyable the object to be destroyed
	 */
	public synchronized void destroyWhenDone(Destroyable destroyable) { 
		if (allreadyDestroyed) {
			//logger.log(Severity.WARN, "Trying to register destroyable["+destroyable+"] on allready destroyed context, destroying it now");
			destroyable.destroy();
		}
		destroyables.add(destroyable); 
	}
	public void destroyWhenDone(int node) { destroyWhenDone(new NomNode(node));	}

	synchronized public void destroy() {
		changeResourcePool((ResourcePool) null);
		if (allreadyDestroyed)
			throw new RuntimeException("Trying to destroy allready destroyed context");
		allreadyDestroyed=true;
		for(Destroyable d:destroyables)
			d.destroy();
	}

	public synchronized void changeResourcePool(String poolName) {
		ResourcePool pool =	relayConnector.getResourcePool(poolName);
		changeResourcePool(pool);
	}
	public synchronized void changeResourcePool(ResourcePool pool) {
		if (resourcepool!=null)
			resourcepool.remove(this);
		resourcepool=pool;
		if (resourcepool!=null)
			resourcepool.add(this);
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
				trace(Severity.WARN, new RelayTrace.Item("Result of methodcall for "+resultVar+" returned Fault: ",responseBody));
			throw new RelayedSoapFaultException(response);
		}
	}
	public Object getObject(String name)  { return objects.get(name); }
	public void setObject(String name, Object o) { objects.put(name, o); }

	
}
