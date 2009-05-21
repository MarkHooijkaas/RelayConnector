package org.kisst.cordys.http;

import org.kisst.cfg4j.MultiSetting;
import org.kisst.cordys.relay.RelayConnector;
import org.kisst.cordys.relay.RelaySettings;
import org.kisst.cordys.script.GenericCommand;
import org.kisst.cordys.script.commands.CommandList;

import com.eibus.soap.Processor;

public class HttpConnector extends RelayConnector {
    public  static final String CONNECTOR_NAME = "HttpConnector";

	public static class Settings extends MultiSetting {
		public RelaySettings relay = new RelaySettings(this);
		public HttpSettings  http  = new HttpSettings(this);
		public Settings(MultiSetting parent) { super(parent, null); }
	}

	public final Settings settings=new Settings(null);
    public HttpConnector() {
    	CommandList.addBasicCommand("http",  new GenericCommand(HttpStep.class));
    	CommandList.addBasicCommand("http-relay",  new GenericCommand(HttpRelayStep.class));
    	CommandList.addBasicCommand("http-callback",  new GenericCommand(HttpCallbackStep.class));
    }
    protected String getManagedComponentType() { return "HttpConnector"; }
    protected String getManagementName() { return "HttpConnector"; }
    
	@Override
    public void open(Processor processor) {
    	super.open(processor);
    	settings.set(conf.properties);
    	if (settings.http.wireLogging.get()!=null) {
    		setLogger("httpclient.wire", settings.http.wireLogging.get());
    		setLogger("org.kisst.cordys.relay.RelayTransaction", settings.http.wireLogging.get()); // TODO: better settings mechanism
    	}
	}

	@Override
	public void reset(Processor processor) {
		super.reset(processor);
    	settings.set(conf.properties);
    	if (settings.http.wireLogging.get()!=null) {
    		setLogger("httpclient.wire", settings.http.wireLogging.get());
    		setLogger("org.kisst.cordys.relay.RelayTransaction", settings.http.wireLogging.get()); // TODO: better settings mechanism
    	}
	}
	
	private void setLogger(String loggerName, String levelName) {
		try {
			// dirty trick, use reflection, so no dependency is on log4j libraries
			// this will prevent linkage errors if log4j is not present.
			Object level = Class.forName("org.apache.log4j.Level").getField(levelName).get(null);
			Object logger = Class.forName("org.apache.log4j.Logger").getMethod("getLogger", new Class[] {String.class} ).invoke(null, new Object[] {loggerName});
			logger.getClass().getMethod("setLevel", new Class[] { level.getClass()}).invoke(logger, new Object[] { level });
		} catch (Exception e) { throw new RuntimeException(e); /* ignore, log4j is not working */		}
	}
}
