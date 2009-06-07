package org.kisst.cordys.http;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.relay.Module;
import org.kisst.cordys.script.GenericCommand;
import org.kisst.cordys.script.commands.CommandList;

public class HttpModule implements Module {
	//private static MultiLevelSettings<HttpSettings> settings=null;

	//public static HttpSettings getSettings(String key) {return settings.get(key); }
	//public static HttpSettings getGlobalSettings() {return settings.getGlobalSettings(); }

	public String getName() { return "HttpModule";	}

	public void init(Props  props) {
    	CommandList.addBasicCommand("http",  new GenericCommand(HttpStep.class));
    	CommandList.addBasicCommand("http-relay",  new GenericCommand(HttpRelayStep.class));
    	CommandList.addBasicCommand("http-callback",  new GenericCommand(HttpCallbackStep.class));
    	reset(props);
	}

	public void reset(Props  props) {
    	//settings=new MultiLevelSettings<HttpSettings>(mlprops,"http", HttpSettings.class);
	}

	public void destroy() {}
}
