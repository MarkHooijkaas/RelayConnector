package org.kisst.cordys.relay;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.TopScript;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.soap.MethodDefinition;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class RelayTransaction  implements ApplicationTransaction
{
	private static final CordysLogger logger = CordysLogger.getCordysLogger(RelayTransaction.class);

	private final RelayConnector connector;
	
	public RelayTransaction(RelayConnector connector) {
		this.connector=connector;
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
    	if (logger.isInfoEnabled()) {
    		logger.log(Severity.INFO, "Received request:\n"+Node.writeToString(Node.getParent(Node.getParent(request.getXMLNode())), true));
    	}
		MethodDefinition def = request.getMethodDefinition();
		ExecutionContext context=new ExecutionContext(connector, request, response);
    	try {
    		TopScript script=getScript(def);
    		script.executeStep(context);
    	}
    	catch (SoapFaultException e) {
    		e.createResponse(response);
    	}
    	catch (Exception e) {
    		int node=response.createSOAPFault("UnknownError",e.toString());
    		StringWriter sw = new StringWriter();
    		e.printStackTrace(new PrintWriter(sw));
    		String details= sw.toString();
    		if (details!=null) {
    			Node.createTextElement("details", details, node);
    		}
    	}
    	if (logger.isInfoEnabled()) {
    		logger.log(Severity.INFO, "Replied with response:\n"+Node.writeToString(Node.getParent(Node.getParent(response.getXMLNode())), true));
    	}
        return true; // connector has to send the response
    }
    
	private TopScript getScript(MethodDefinition def) {
		String methodName=def.getNamespace()+"/"+def.getMethodName();
		TopScript script=connector.scriptCache.get(methodName);
		if (script==null) {
			script=new TopScript(connector, def);
			if (RelayModule.getSettings().cacheScripts.get())
				connector.scriptCache.put(methodName, script);
		}
		return script;
	}

}
