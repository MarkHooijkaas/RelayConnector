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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.connector.BaseSettings;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Script;
import org.kisst.cordys.util.DnUtil;
import org.kisst.cordys.util.NomUtil;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.soap.SOAPTransaction;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class RelayTransaction implements ApplicationTransaction
{
	private static final CordysLogger logger = CordysLogger.getCordysLogger(RelayTransaction.class);

	private final Props props;
	private final ExecutionContext context;

	public RelayTransaction(RelayConnector connector, String fullMethodName, Props props, SOAPTransaction transaction) {
    	context=new ExecutionContext(connector, fullMethodName, props, transaction);
		this.props=props;
	}

	public boolean canProcess(String callType) {
    	if ("RelayCall".equals(callType))
    		return true;
    	else
    		return false;
    }
    
    public void commit() {}
    public void abort() {}

    /**
     * This method processes the received request. 
     * @return true if the connector has to send the response. 
     *         If someone else sends the response false is returned.
     */
    public boolean process(BodyBlock request, BodyBlock response) {
    	final Date startTime=new Date();
    	String user=DnUtil.getFirstDnPart(context.getOrganizationalUser());
    	final Monitor mon1 = MonitorFactory.start("IncomingCall:"+NomUtil.getUniversalName(request.getXMLNode()));
		final Monitor mon2 = MonitorFactory.start("AllIncomingCalls");
    	final Monitor monu1 = MonitorFactory.start("IncomingCallForUser:"+user+":"+NomUtil.getUniversalName(request.getXMLNode()));
		final Monitor monu2 = MonitorFactory.start("AllIncomingCallsForUser:"+user);
    	if (BaseSettings.emergencyBreak.get(props)) {
    		String msg="Forbidden to call method "+context.fullMethodName+", see relay.forbidden property";
    		logger.log(Severity.WARN, msg);
    		response.createSOAPFault("ESB.TECHERR.FORBIDDEN",msg);
    		return true;
    	}
    		
    	context.setCallDetails(request, response);
		int impl = request.getMethodDefinition().getImplementation();
		String result="ERROR ";
    	try {
    		String pool= BaseSettings.resourcepool.get(props);
    		if (pool!=null)
    			context.changeResourcePool(pool);
        	Script script=compileScript(impl);
        	if (context.infoTraceEnabled()) {
        		int reqnode=request.getXMLNode();
        		if (BaseSettings.traceShowEnvelope.get(props))
        			reqnode=NomUtil.getRootNode(reqnode);
        		context.traceInfo("Received request: ",reqnode);
        	}
    		script.executeStep(context);
        	if (context.infoTraceEnabled()) {
        		int respnode=response.getXMLNode();
        		if (BaseSettings.traceShowEnvelope.get(props))
        			respnode=NomUtil.getRootNode(respnode);
        		context.traceInfo("Replied with response: ", respnode);
        	}
        	result="SUCCESS ";
    	}
    	catch (RelayedSoapFaultException e) {
    		Severity sev=BaseSettings.logRelayedSoapFaults.get(props);
    		if (sev!=null)
    			RelayTrace.logger.log(sev, "Relaying Soapfault "+e.getMessage());
    		e.createResponse(response);
    	}
    	catch (SoapFaultException e) {
    		int soapfault=response.createSOAPFault(e.getFaultcode(), e.getFaultstring());
    		createErrorDetails(soapfault, context, e);
   			e.fillDetails(soapfault);
    	}
    	catch (Throwable e) { //catch Throwable to also catch NoClassDefError
    		String prefix=BaseSettings.soapFaultcodePrefix.get(props);
    		NomUtil.deleteChildren(response.getXMLNode());// TODO: is this still necessary in C3?
    		int soapfault=response.createSOAPFault(prefix+e.getClass().getSimpleName(), e.getMessage());
    		createErrorDetails(soapfault, context, e);
    	}
    	finally {
   			context.destroy();
			mon1.stop();
			mon2.stop();
			monu1.stop();
			monu2.stop();
			context.getBaseConnector().logPerformance("INCOMING", context, startTime, request.getXMLNode(), "OK".equals(result));
    	}
		if (context.getTimer()!=null)
			context.getTimer().log(" finished "+result+context.getFullMethodName());
		int sleep=BaseSettings.sleepAfterCall.get(props);
		if (sleep>0) {
			RelayTrace.logger.log(Severity.WARN, "Sleeping for "+sleep+" milliseconds");
		  	try {
				Thread.sleep(BaseSettings.sleepAfterCall.get(props));
			} catch (InterruptedException e) { throw new RuntimeException(e); }
		}
		return true; // connector has to send the response
    }

	private int createErrorDetails(int details, ExecutionContext context, Throwable e) {
		String msg=e.getMessage()+" while handling "+context.getFullMethodName();
		if (BaseSettings.logRequestOnError.get(context.props)) {
			int input=context.getXmlVar("input");
			input=NomUtil.getRootNode(input);
			msg+="\n"+Node.writeToString(input,false);
		}
		if (BaseSettings.logTrace.get(props))
			msg+="\n"+context.getTraceAsString(props);
		RelayTrace.logger.log(Severity.ERROR, msg, e);
		if (BaseSettings.showStacktrace.get(props))
			Node.createTextElement("stacktrace", getStackTraceAsString(e), details);
		if (BaseSettings.showTrace.get(props))
			context.addToNode(Node.createElement("trace", details), props);
		return details;
	}

	private String getStackTraceAsString(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	private Script compileScript(int node) {
		Script script=RelayModule.scriptCache.get(context.getFullMethodName());
		if (script==null) {
			script=new Script(node, props);
			if (BaseSettings.cacheScripts.get(props))
				RelayModule.scriptCache.put(context.getFullMethodName(), script);
		}
		return script;
	}
}
