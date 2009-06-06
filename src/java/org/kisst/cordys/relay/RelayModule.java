package org.kisst.cordys.relay;

import org.kisst.cfg4j.Props;

public class RelayModule implements Module {
	private static RelaySettings settings=null;

	public static RelaySettings getSettings() {
		return settings;
	}

	public String getName() { return "RelayModule";	}

	public void init(Props props) {
    	reset(props);
	}

	public void reset(Props props) {
		settings = new RelaySettings(props, "relay");
	}

	public void destroy() {}
}
