package org.kisst.cordys.relay;

import org.kisst.cordys.script.Script;

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
    	try {
    		Script script=connector.getScript(def);
    		script.execute(connector, request, response);
    	}
    	catch (SoapFaultException e) {
    		e.createResponse(response);
    	}
    	if (logger.isInfoEnabled()) {
    		logger.log(Severity.INFO, "Replied with response:\n"+Node.writeToString(Node.getParent(Node.getParent(response.getXMLNode())), true));
    	}
        return true; // connector has to send the response
    }
}
