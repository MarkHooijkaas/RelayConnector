package org.kisst.cordys.http;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.relay.Module;
import org.kisst.cordys.script.GenericCommand;
import org.kisst.cordys.script.commands.CommandList;

public class HttpModule implements Module {
	private static HttpSettings settings=null;

	public static HttpSettings getSettings() {
		return settings;
	}

	public String getName() { return "HttpModule";	}

	public void init(Props props) {
    	CommandList.addBasicCommand("http",  new GenericCommand(HttpStep.class));
    	CommandList.addBasicCommand("http-relay",  new GenericCommand(HttpRelayStep.class));
    	CommandList.addBasicCommand("http-callback",  new GenericCommand(HttpCallbackStep.class));
    	reset(props);
	}

	public void reset(Props props) {
    	settings=new HttpSettings(props,"http");
	}

	public void destroy() {}
}
