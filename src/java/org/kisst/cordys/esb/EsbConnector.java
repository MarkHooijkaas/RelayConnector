package org.kisst.cordys.esb;

import org.kisst.cordys.util.BaseConnector;
import org.kisst.cordys.util.NomUtil;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.SOAPTransaction;

public class EsbConnector extends BaseConnector {
    public EsbConnector(){
    }
	@Override
	protected String getConnectorName() { return "EsbConnector"; }
    
    public ApplicationTransaction createTransaction(SOAPTransaction stTransaction) {
    	int env=stTransaction.getRequestEnvelope();
    	int req=SoapUtil.getContent(env);
    	String key=NomUtil.getUniversalName(req);
		return new EsbTransaction(mlprops.getProps(key));
	}

}
