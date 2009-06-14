package org.kisst.cordys.relay;

import java.util.HashMap;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.script.Script;

public class RelayModule implements Module {
	static final HashMap<String, Script> scriptCache = new HashMap<String, Script>();

	public String getName() { return "RelayModule";	}

	public void init(Props props) {
    	reset(props);
	}

	public void reset(Props props) {
		scriptCache.clear();
	}

	public void destroy() {}
}
