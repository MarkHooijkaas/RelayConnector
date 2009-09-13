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

import org.kisst.cfg4j.Props;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Script;
import org.kisst.cordys.util.NomUtil;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class RelayTransaction implements ApplicationTransaction
{
	private final CallContext ctxt;
	private final Props props;
	
	public RelayTransaction(CallContext ctxt) {
		this.ctxt=ctxt;
		this.props=ctxt.getProps();
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
		ctxt.traceInfo("Received request: ",request.getXMLNode());
		int impl = request.getMethodDefinition().getImplementation();
		ExecutionContext context=null;
		String result="ERROR ";
    	try {
        	Script script=compileScript(impl);
        	context=new ExecutionContext(ctxt, request, response);
    		script.executeStep(context);
   			ctxt.traceInfo("Replied with response: ", response.getXMLNode());
        	result="SUCCESS ";
    	}
    	catch (RelayedSoapFaultException e) {
    		Severity sev=RelaySettings.logRelayedSoapFaults.get(props);
    		if (sev!=null)
    			RelayTrace.logger.log(sev, "Relaying Soapfault "+e.getMessage());
    		e.createResponse(response);
    	}
    	catch (SoapFaultException e) {
    		int details=response.createSOAPFault(e.getFaultcode(), e.getFaultstring());
    		createErrorDetails(details, e);
   			e.fillDetails(details);
    	}
    	catch (Throwable e) { //catch Throwable to also catch NoClassDefError
    		String prefix=RelaySettings.soapFaultcodePrefix.get(props);
    		NomUtil.deleteChildren(response.getXMLNode());// TODO: is this still necessary in C3?
    		int details=response.createSOAPFault(prefix+e.getClass().getSimpleName(), e.getMessage());
    		createErrorDetails(details, e);
    	}
    	finally {
   			ctxt.destroy();
    	}
		if (ctxt.getTimer()!=null)
			ctxt.getTimer().log(" finished "+result+ctxt.getFullMethodName());
		int sleep=RelaySettings.sleepAfterCall.get(props);
		if (sleep>0) {
			RelayTrace.logger.log(Severity.WARN, "Sleeping for "+sleep+" seconds");
		  	try {
				Thread.sleep(RelaySettings.sleepAfterCall.get(props));
			} catch (InterruptedException e) { throw new RuntimeException(e); }
		}
		return true; // connector has to send the response
    }

	private int createErrorDetails(int details, Throwable e) {
		String msg=e.getMessage();
		boolean trace=RelaySettings.trace.get(props);
		if (trace && RelaySettings.logTrace.get(props))
			msg+="\n"+ctxt.getTrace().getTraceAsString(props);
		RelayTrace.logger.log(Severity.ERROR, msg, e);
		if (RelaySettings.showStacktrace.get(props))
			Node.createTextElement("stacktrace", getStackTraceAsString(e), details);
		if (trace && RelaySettings.showTrace.get(props))
			ctxt.getTrace().addToNode(Node.createElement("trace", details), props);
		return details;
	}

	private String getStackTraceAsString(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	private Script compileScript(int node) {
		Script script=RelayModule.scriptCache.get(ctxt.getFullMethodName());
		if (script==null) {
			script=new Script(ctxt, node);
			if (RelaySettings.cacheScripts.get(props))
				RelayModule.scriptCache.put(ctxt.getFullMethodName(), script);
		}
		return script;
	}
}
