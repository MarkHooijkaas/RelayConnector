package org.kisst.cordys.http;

import org.kisst.cordys.relay.RelayConnector;

public class HttpConnector extends RelayConnector {
    public  static final String CONNECTOR_NAME = "HttpConnector";
	private HttpModule httpModule= new HttpModule();

    public HttpConnector(){
    	addModule(httpModule);
    }
    
    protected String getManagedComponentType() { return "HttpConnector"; }
    protected String getManagementName() { return "HttpConnector"; }
}
