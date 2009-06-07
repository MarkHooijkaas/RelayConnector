package org.kisst.cordys.relay;

import org.kisst.cfg4j.MultiLevelProps;
import org.kisst.cfg4j.MultiLevelSettings;

public class RelayModule implements Module {
	private static MultiLevelSettings<RelaySettings> settings=null;

	public static RelaySettings getGlobalSettings()     { return settings.getGlobalSettings(); }
	public static RelaySettings getSettings(String key) { return settings.get(key); }

	public String getName() { return "RelayModule";	}

	public void init(MultiLevelProps props) {
    	reset(props);
	}

	public void reset(MultiLevelProps mlprops) {
    	settings=new MultiLevelSettings<RelaySettings>(mlprops, "relay", RelaySettings.class);
	}

	public void destroy() {}
}
