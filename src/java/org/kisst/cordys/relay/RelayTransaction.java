package org.kisst.cordys.relay;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.RelayTrace;
import org.kisst.cordys.script.Script;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class RelayTransaction extends CallContext implements ApplicationTransaction
{
	private Script script;
	
	public Script getScript() { return script;}
	
	public RelayTransaction(RelayConnector connector, String methodName, Props props) {
		super(connector, methodName, props);
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
        	context=new ExecutionContext(this, request, response);
        	if (context.infoTraceEnabled())
        		context.traceInfo("Received request:\n"+Node.writeToString(Node.getParent(Node.getParent(request.getXMLNode())), true));
    		script.executeStep(context);
        	if (context.infoTraceEnabled())
        		context.traceInfo("Replied with response:\n"+Node.writeToString(Node.getParent(Node.getParent(response.getXMLNode())), true));
    	}
    	catch (SoapFaultException e) {
    		e.createResponse(response, props);
    	}
    	catch (Exception e) {
    		RelayTrace.logger.log(Severity.ERROR, "Error", e);
    		int node=response.createSOAPFault("TECHERR.ESB",e.getMessage());
    		if (RelaySettings.showStacktrace.get(props)) {
    			StringWriter sw = new StringWriter();
    			e.printStackTrace(new PrintWriter(sw));
    			String details= sw.toString();
    			if (details!=null) {
    				Node.createTextElement("details", details, node);
    			}
    		}
    	}
    	finally {
    		if (context!=null)
    			context.destroy();
    	}
		if (timer!=null)
			timer.log(" finished "+fullMethodName);
		return true; // connector has to send the response
    }
    
	private void compileScript(int node) {
		script=relayConnector.scriptCache.get(fullMethodName);
		if (script==null) {
			script=new Script(this, node);
			if (RelaySettings.cacheScripts.get(props))
				relayConnector.scriptCache.put(fullMethodName, script);
		}
	}

}
