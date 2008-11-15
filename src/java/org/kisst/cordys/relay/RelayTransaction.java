package org.kisst.cordys.relay;

import org.kisst.cordys.script.Script;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.soap.MethodDefinition;

public class RelayTransaction  implements ApplicationTransaction
{
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
		MethodDefinition def = request.getMethodDefinition();
    	try {
    		Script script=connector.getScript(def);
    		script.execute(connector, request, response);
    	}
    	catch (SoapFaultException e) {
    		// TODO: add detail and actor info
    		response.createSOAPFault(e.code,e.message);
    	}
        return true; // connector has to send the response
    }
}
