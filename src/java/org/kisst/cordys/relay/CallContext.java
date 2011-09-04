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
import org.kisst.cordys.connector.BaseConnector;
import org.kisst.cordys.connector.BaseSettings;
import org.kisst.cordys.connector.MethodCache;
import org.kisst.cordys.connector.resourcepool.ResourcePool;
import org.kisst.cordys.util.Destroyable;
import org.kisst.cordys.util.DnUtil;
import org.kisst.cordys.util.NomNode;
import org.kisst.cordys.util.NomUtil;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.connector.nom.Connector;
import com.eibus.directory.soap.DirectoryException;
import com.eibus.soap.SOAPTransaction;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class CallContext extends RelayTrace {
	protected final ArrayList<Destroyable> destroyables = new ArrayList<Destroyable>();
	private Exception asynchronousError=null;
	protected boolean allreadyDestroyed=false;

	
	protected final BaseConnector baseConnector;
	protected final String fullMethodName;
	protected final Props props;
	protected final RelayTimer timer;
	private final SOAPTransaction soapTransaction;
	private ResourcePool resourcepool=null;
	
	private final HashMap<String,Object> objects=new HashMap<String,Object>();

	public BaseConnector getBaseConnector() { return baseConnector; }
	public String getFullMethodName() {	return fullMethodName;	}
	public Props getProps() { return props;	}
	public RelayTrace getTrace() {return this;} // TODO: remove
	public RelayTimer getTimer() { return timer;}
	public String getOrganization() { return baseConnector.getOrganization(); }	
	public String getOrganizationalUser() {	return soapTransaction.getUserCredentials().getOrganizationalUser();}
	public Document getDocument() { return Node.getDocument(soapTransaction.getRequestEnvelope()); } // TODO: is this the best document?
	public boolean allreadyDestroyed() { return allreadyDestroyed; }

	public CallContext(BaseConnector connector, String fullMethodName, Props props, SOAPTransaction stTransaction) {
		super(BaseSettings.trace.get(props));
		this.baseConnector=connector;
		this.fullMethodName=fullMethodName;
		this.props=props;
		this.soapTransaction=stTransaction;

		if (BaseSettings.timer.get(props))
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
		ResourcePool pool =	baseConnector.getResourcePool(poolName);
		changeResourcePool(pool);
	}
	public synchronized void changeResourcePool(ResourcePool pool) {
		if (this.resourcepool!=null)
			this.resourcepool.remove(this);
		this.resourcepool=pool;
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
		Connector connector = getBaseConnector().getConnector();
		try {
			int method = connector.createSOAPMethod(dnUser, dnOrganization, namespace, methodname);
			if (method!=0) {
				destroyWhenDone(new NomNode(Node.getParent(Node.getParent(method))));
				return new NomNode(method);
			}
			else
				return null;

		} 
		catch (DirectoryException e) {  throw new RuntimeException("Error when creating method call for "+methodname,e); }
	}

	public int callMethod(int method, String resultVar) { return callMethod(method, resultVar, false); }

	public int callMethod(int method, String resultVar, boolean ignoreSoapFault) {
    	String user=DnUtil.getFirstDnPart(getOrganizationalUser());
		traceInfo("sending request: ", method);
		MethodCache caller = getBaseConnector().responseCache;
		Monitor mon1 = MonitorFactory.start("OutgoingCall:"+NomUtil.getUniversalName(method));
		Monitor mon2 = MonitorFactory.start("AllOutgoingCalls");
		Monitor monu1 = MonitorFactory.start("OutgoingCallForUser:"+user+":"+NomUtil.getUniversalName(method));
		Monitor monu2 = MonitorFactory.start("AllOutgoingCallsForUser:"+user);
		int response = caller.sendAndWait(method,BaseSettings.timeout.get(getProps()));
		mon1.stop();
		mon2.stop();
		monu1.stop();
		monu2.stop();
		checkAndLogResponse(response, resultVar, ignoreSoapFault);
		return response;
	}
	
	public void checkAndLogResponse(int response, String resultVar, boolean ignoreSoapFault) {
		destroyWhenDone(new NomNode(response));
		int responseBody=SoapUtil.getContent(response);
		if (infoTraceEnabled()) 
			// TODO: this might fail if context is already done and response is deleted
			traceInfo("received response: ",responseBody);
		if (allreadyDestroyed)
			return;
		if ((! ignoreSoapFault) && SoapUtil.isSoapFault(responseBody)) {
				trace(Severity.WARN, new RelayTrace.Item("Result of methodcall for "+resultVar+" returned Fault: ",responseBody));
			throw new RelayedSoapFaultException(response);
		}
	}
	public Object getObject(String name)  { return objects.get(name); }
	public void setObject(String name, Object o) { objects.put(name, o); }

	
}
