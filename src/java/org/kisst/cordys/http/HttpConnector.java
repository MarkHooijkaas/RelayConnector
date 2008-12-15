package org.kisst.cordys.http;

import org.kisst.cfg4j.MultiSetting;
import org.kisst.cordys.relay.RelayConnector;
import org.kisst.cordys.relay.RelaySettings;
import org.kisst.cordys.script.GenericCommand;
import org.kisst.cordys.script.commands.CommandList;

public class HttpConnector extends RelayConnector {
    public  static final String CONNECTOR_NAME = "HttpConnector";

	public static class Settings extends MultiSetting {
		public RelaySettings relay = new RelaySettings(this);
		public HttpSettings  http  = new HttpSettings(this);
		public Settings(MultiSetting parent) { super(parent, null); }
	}

	public final Settings settings=new Settings(null);
    public HttpConnector() {
    	CommandList.getBasicCommands().addCommand("http",  new GenericCommand(HttpStep.class));
    }
    protected String getManagedComponentType() { return "HttpConnector"; }
    protected String getManagementName() { return "HttpConnector"; }
}
