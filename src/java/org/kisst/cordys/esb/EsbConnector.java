package org.kisst.cordys.esb;

import org.kisst.cordys.relay.RelayConnector;
import org.kisst.cordys.util.NomUtil;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.SOAPTransaction;

public class EsbConnector extends RelayConnector {
    public  static final String CONNECTOR_NAME = "EsbConnector";

    public EsbConnector(){
    }
    
    protected String getManagedComponentType() { return "EsbConnector"; }
    protected String getManagementName() { return "EsbConnector"; }
    
    public ApplicationTransaction createTransaction(SOAPTransaction stTransaction) {
    	int env=stTransaction.getRequestEnvelope();
    	int req=SoapUtil.getContent(env);
    	String key=NomUtil.getUniversalName(req);
		return new EsbTransaction(mlprops.getProps(key));
	}
}
