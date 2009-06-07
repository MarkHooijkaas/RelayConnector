package org.kisst.cordys.relay;

import org.kisst.cfg4j.Props;

public interface Module {
	public String getName();
	public void init(Props props);
	public void reset(Props props);
	public void destroy();
}
