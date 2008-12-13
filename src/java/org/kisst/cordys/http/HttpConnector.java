package org.kisst.cordys.relay;

import org.kisst.cordys.http.HttpStep;
import org.kisst.cordys.script.GenericCommand;
import org.kisst.cordys.script.commands.CommandList;

public class HttpConnector extends RelayConnector {
	//private static final CordysLogger logger = CordysLogger.getCordysLogger(RelayConnector.class);
    public  static final String CONNECTOR_NAME = "RelayConnector";

    public HttpConnector() {
    	CommandList.getBasicCommands().addCommand("http",  new GenericCommand(HttpStep.class));
    }
    protected String getManagedComponentType() { return "HttpConnector"; }
    protected String getManagementName() { return "HttpConnector"; }
}
