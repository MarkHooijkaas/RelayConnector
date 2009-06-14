package org.kisst.cordys.relay;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Script;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class RelayTransaction implements ApplicationTransaction
{
	private final CallContext ctxt;
	
	public RelayTransaction(CallContext ctxt) {
		this.ctxt=ctxt;
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
		if (ctxt.infoTraceEnabled())
			ctxt.traceInfo("Received request: "+Node.writeToString(Node.getParent(Node.getParent(request.getXMLNode())), false));
		int impl = request.getMethodDefinition().getImplementation();
		ExecutionContext context=null;
		String result="ERROR ";
    	try {
        	Script script=compileScript(impl);
        	context=new ExecutionContext(ctxt, request, response);
    		script.executeStep(context);
    		if (ctxt.infoTraceEnabled())
    			ctxt.traceInfo("Replied with response: "+Node.writeToString(Node.getParent(Node.getParent(response.getXMLNode())), false));
        	result="SUCCESS ";
    	}
    	catch (RelayedSoapFaultException e) {
    		Severity sev=RelaySettings.logRelayedSoapFaults.get(ctxt.getProps());
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
    		int details=response.createSOAPFault("TECHERR.ESB."+e.getClass().getName(), e.getMessage());
    		createErrorDetails(details, e);
    	}
    	finally {
   			ctxt.destroy();
    	}
		if (ctxt.getTimer()!=null)
			ctxt.getTimer().log(" finished "+result+ctxt.getFullMethodName());
		int sleep=RelaySettings.sleepAfterCall.get(ctxt.getProps());
		if (sleep>0) {
			RelayTrace.logger.log(Severity.WARN, "Sleeping for "+sleep+" seconds");
		  	try {
				Thread.sleep(RelaySettings.sleepAfterCall.get(ctxt.getProps()));
			} catch (InterruptedException e) { throw new RuntimeException(e); }
		}
		return true; // connector has to send the response
    }

	private int createErrorDetails(int details, Throwable e) {
		Props props= ctxt.getProps();
		String msg=e.getMessage();
		boolean trace=RelaySettings.trace.get(props);
		if (trace && RelaySettings.logTrace.get(props))
			msg+="\n"+ctxt.getTrace().getTraceAsString();
		RelayTrace.logger.log(Severity.ERROR, msg, e);
		if (RelaySettings.showStacktrace.get(ctxt.getProps()))
			Node.createTextElement("stacktrace", getStackTraceAsString(e), details);
		if (trace && RelaySettings.showTrace.get(props))
			ctxt.getTrace().addToNode(Node.createElement("trace", details));
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
			if (RelaySettings.cacheScripts.get(ctxt.getProps()))
				RelayModule.scriptCache.put(ctxt.getFullMethodName(), script);
		}
		return script;
	}
}
