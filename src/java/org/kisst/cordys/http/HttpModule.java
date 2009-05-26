package org.kisst.cordys.http;

import java.util.Properties;

import org.kisst.cordys.relay.Module;
import org.kisst.cordys.script.GenericCommand;
import org.kisst.cordys.script.commands.CommandList;

public class HttpModule implements Module {
	private static HttpSettings settings=null;

	public static HttpSettings getSettings() {
		return settings;
	}

	public String getName() { return "HttpModule";	}

	public void init(Properties properties) {
    	CommandList.addBasicCommand("http",  new GenericCommand(HttpStep.class));
    	CommandList.addBasicCommand("http-relay",  new GenericCommand(HttpRelayStep.class));
    	CommandList.addBasicCommand("http-callback",  new GenericCommand(HttpCallbackStep.class));
    	reset(properties);
	}

	public void reset(Properties properties) {
    	settings=new HttpSettings(null,"http",properties);
    	if (settings.wireLogging.get()!=null) {
    		setLogger("httpclient.wire", settings.wireLogging.get());
    		setLogger("org.kisst.cordys.relay.RelayTrace", settings.wireLogging.get()); // TODO: better settings mechanism
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

	public void destroy() {}
}
