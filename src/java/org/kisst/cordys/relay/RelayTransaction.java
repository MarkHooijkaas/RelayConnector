package org.kisst.cordys.relay;

import java.io.PrintWriter;
import java.io.StringWriter;

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
		int impl = request.getMethodDefinition().getImplementation();
		ExecutionContext context=null;
		String result="ERROR ";
    	try {
        	Script script=compileScript(impl);
        	context=new ExecutionContext(ctxt, request, response);
        	runscript(script, context);
        	result="SUCCESS ";
    	}
    	catch (RelayedSoapFaultException e) {
    		Severity sev=RelaySettings.logRelayedSoapFaults.get(ctxt.getProps());
    		if (sev!=null)
    			RelayTrace.logger.log(sev, "Relaying Soapfault "+e.getMessage());
    		e.createResponse(response);
    	}
    	catch (SoapFaultException e) {
    		RelayTrace.logger.log(Severity.ERROR, "Error", e);
    		int node=response.createSOAPFault(e.getFaultcode(), e.getFaultstring());
    		int details=createErrorDetails(node, e);
    		if (e.hasDetails()) {
    			if (details==0)
    				details=Node.createElement("details", node);
    			e.fillDetails(details);
    		}
    	}
    	catch (Exception e) {
    		RelayTrace.logger.log(Severity.ERROR, "Error", e);
    		int node=response.createSOAPFault("TECHERR.ESB."+e.getClass().getName(), e.getMessage());
    		createErrorDetails(node, e);
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

	private int createErrorDetails(int node, Exception e) {
		int details=0;
		if (RelaySettings.showStacktrace.get(ctxt.getProps()))
			details=addErrorDetail(node, details, "stacktrace", getStackTraceAsString(e));
		if (RelaySettings.trace.get(ctxt.getProps()))
			details=addErrorDetail(node, details, "trace", ctxt.getTrace().toString());
		return details;
	}

	private int addErrorDetail(int node, int details, String tag, String msg) {
		if (details==0)
			details=Node.createElement("details", node);
		Node.createTextElement(tag, msg, details);
		return details;
	}
	private String getStackTraceAsString(Exception e) {
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

	private void runscript(Script script, ExecutionContext context) {
		if (context.infoTraceEnabled())
			context.traceInfo("Received request:\n"+Node.writeToString(Node.getParent(Node.getParent(context.getRequest().getXMLNode())), true));
		script.executeStep(context);
		if (context.infoTraceEnabled())
			context.traceInfo("Replied with response:\n"+Node.writeToString(Node.getParent(Node.getParent(context.getResponse().getXMLNode())), true));
	}
    

}
