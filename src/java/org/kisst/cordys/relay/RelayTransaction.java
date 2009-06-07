package org.kisst.cordys.relay;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.TopScript;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.soap.MethodDefinition;
import com.eibus.xml.nom.Node;

public class RelayTransaction  implements ApplicationTransaction
{
	private final RelayConnector connector;
	private final Props props;
	
	public RelayTransaction(RelayConnector connector, Props props) {
		this.connector=connector;
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
		MethodDefinition def = request.getMethodDefinition();
		ExecutionContext context=null;
    	try {
        	TopScript script=getScript(def);
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
        	context.destroy();
    	}
        return true; // connector has to send the response
    }
    
	private TopScript getScript(MethodDefinition def) {
		String methodName=def.getNamespace()+"/"+def.getMethodName();
		TopScript script=connector.scriptCache.get(methodName);
		if (script==null) {
			script=new TopScript(connector, def, props);
			if (RelaySettings.cacheScripts.get(props))
				connector.scriptCache.put(methodName, script);
		}
		return script;
	}

}
