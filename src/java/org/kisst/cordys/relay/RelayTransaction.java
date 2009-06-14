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
	private Script script;
	
	public Script getScript() { return script;}
	
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
    	try {
        	compileScript(impl);
        	context=new ExecutionContext(ctxt, request, response);
        	if (context.infoTraceEnabled())
        		context.traceInfo("Received request:\n"+Node.writeToString(Node.getParent(Node.getParent(request.getXMLNode())), true));
    		script.executeStep(context);
        	if (context.infoTraceEnabled())
        		context.traceInfo("Replied with response:\n"+Node.writeToString(Node.getParent(Node.getParent(response.getXMLNode())), true));
    	}
    	catch (SoapFaultException e) {
    		e.createResponse(response, ctxt.getProps());
    	}
    	catch (Exception e) {
    		RelayTrace.logger.log(Severity.ERROR, "Error", e);
    		int node=response.createSOAPFault("TECHERR.ESB",e.getMessage());
    		if (RelaySettings.showStacktrace.get(ctxt.getProps())) {
    			StringWriter sw = new StringWriter();
    			e.printStackTrace(new PrintWriter(sw));
    			String details= sw.toString();
    			if (details!=null) {
    				Node.createTextElement("details", details, node);
    			}
    		}
    	}
    	finally {
   			ctxt.destroy();
    	}
		if (ctxt.getTimer()!=null)
			ctxt.getTimer().log(" finished "+ctxt.getFullMethodName());
		return true; // connector has to send the response
    }
    
	private void compileScript(int node) {
		script=RelayModule.scriptCache.get(ctxt.getFullMethodName());
		if (script==null) {
			script=new Script(ctxt, node);
			if (RelaySettings.cacheScripts.get(ctxt.getProps()))
				RelayModule.scriptCache.put(ctxt.getFullMethodName(), script);
		}
	}

}
