package org.kisst.cordys.relay;

import java.util.Properties;

public interface Module {
	public String getName();
	public void init(Properties properties);
	public void reset(Properties properties);
	public void destroy();
}
