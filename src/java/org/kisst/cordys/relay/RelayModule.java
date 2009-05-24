package org.kisst.cordys.relay;

import java.util.Properties;

public class RelayModule implements Module {
	private static RelaySettings settings=null;

	public static RelaySettings getSettings() {
		return settings;
	}

	public String getName() { return "RelayModule";	}

	public void init(Properties properties) {
    	reset(properties);
	}

	public void reset(Properties properties) {
		settings = new RelaySettings(null, "relay", properties);
	}

	public void destroy() {}
}
