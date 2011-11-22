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

package org.kisst.cordys.script;

import java.util.Date;
import java.util.HashMap;

import org.kisst.cordys.connector.BaseSettings;
import org.kisst.cordys.connector.CallContext;
import org.kisst.cordys.connector.MethodCache;
import org.kisst.cordys.relay.RelayConnector;
import org.kisst.cordys.util.DnUtil;
import org.kisst.cordys.util.NomUtil;
import org.kisst.cordys.util.SoapUtil;
import org.kisst.props4j.Props;

import com.eibus.connector.nom.SOAPMessageListener;
import com.eibus.soap.BodyBlock;
import com.eibus.soap.SOAPTransaction;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class ExecutionContext extends CallContext {
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

	public ExecutionContext(RelayConnector connector, String fullMethodName, Props props, SOAPTransaction transaction) {
    	super(connector, fullMethodName, props, transaction);
    }

	@Override
	public void setCallDetails(BodyBlock request, BodyBlock response) {
		super.setCallDetails(request, response);
		setXmlVar("input", request.getXMLNode());
		setXmlVar("output", response.getXMLNode());
	}

	synchronized public void createXmlSlot(String name, String method, long timeoutTime) {
		xmlvars.put(name, new XmlVar(method, timeoutTime));
	}
	synchronized public void setXmlVar(String name, int node) {
		xmlvars.put(name, new XmlVar(node));
		this.traceInfo("Setting xmlvar "+name, node);
		this.notifyAll(); // notify should suffice as well instead of notifyAll
	}

	synchronized public void setTextVar(String name, String value) {
		if (allreadyDestroyed())
			logger.log(Severity.WARN, "Trying to set text var ["+name+"] on allready destroyed context to value ["+value+"]");
		this.traceInfo("Setting textvar "+name+" to "+value);
		textvars.put(name, new TextVar(value));
	}

	synchronized public String getTextVar(String name) {
		checkForException();
		TextVar v = textvars.get(name);
		if (v==null)
			throw new RuntimeException("Unknown TextVar "+name);
		return v.str;
	}

	synchronized public int getXmlVar(String name) {
		while (true) {
			if (allreadyDestroyed())
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

	// TODO: fix possible memory leak if async calls never return a answer
	// Such call will register a SOAPListener which will never be removed.
	// In long term this could lead to a OutOfMemory error.
	// Possible fix would be to not use sendAndCallback, but just send, and use a 
	// default SOAPListener. This in considered not very urgent yet.

	public void callMethodAsync(int method, final String resultVar) { callMethodAsync(method, resultVar, false); }
	
	public void callMethodAsync(final int method, final String resultVar, final boolean ignoreSoapFault) {
		traceInfo("sending request: ",method);
		String methodName=Node.getLocalName(method);
		MethodCache caller = getBaseConnector().responseCache;
		createXmlSlot(resultVar, methodName, new Date().getTime()+BaseSettings.timeout.get(getProps()));
    	String user=DnUtil.getFirstDnPart(getOrganizationalUser());
		final Monitor mon1 = MonitorFactory.start("OutgoingCall:"+NomUtil.getUniversalName(method));
		final Monitor mon2 = MonitorFactory.start("AllOutgoingCalls");
		final Monitor monu1 = MonitorFactory.start("OutgoingCallForUser:"+user+":"+NomUtil.getUniversalName(method));
		final Monitor monu2 = MonitorFactory.start("AllOutgoingCallsForUser:"+user);
    	final Date startTime=new Date();
		caller.sendAndCallback(Node.getParent(method),new SOAPMessageListener() {
			public boolean onReceive(int message)
			{
				try {
					getBaseConnector().logPerformance("CALL-ASYNC", ExecutionContext.this, startTime, method, true);
					mon1.stop();
					mon2.stop();
					monu1.stop();
					monu2.stop();
					checkAndLogResponse(message, resultVar, ignoreSoapFault);
					setXmlVar(resultVar, SoapUtil.getContent(message));
				}
				catch(Exception e) {
					setAsynchronousError(e);
				}
				return false; // Node should not yet be destroyed by Callback caller!!
			}
		});
	}
}
