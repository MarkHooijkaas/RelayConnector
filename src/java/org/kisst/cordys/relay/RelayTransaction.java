package org.kisst.cordys.relay;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.RelayTrace;
import org.kisst.cordys.script.TopScript;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.soap.MethodDefinition;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class RelayTransaction  implements ApplicationTransaction
{
	private final RelayConnector connector;
	private final String fullMethodName;
	private final Props props;
	private final RelayTimer timer;
	
	public RelayTransaction(RelayConnector connector, String methodName, Props props) {
		this.connector=connector;
		this.fullMethodName=methodName;
		this.props=props;
		if (RelaySettings.timer.get(props))
			timer=new RelayTimer();
		else
			timer=null;
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
		MethodDefinition def = request.getMethodDefinition();
		ExecutionContext context=null;
    	try {
        	TopScript script=getScript(def.getImplementation());
        	context=new ExecutionContext(script, connector, request, response);
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
    		int node=response.createSOAPFault("UnknownError",e.toString());
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
    	if (timer!=null) {
    		timer.log("");
    	}
        return true; // connector has to send the response
    }
    
	private TopScript getScript(int node) {
		TopScript script=connector.scriptCache.get(fullMethodName);
		if (script==null) {
			script=new TopScript(connector, node, props);
			if (RelaySettings.cacheScripts.get(props))
				connector.scriptCache.put(fullMethodName, script);
		}
		return script;
	}

}
