package org.kisst.cordys.relay;

import org.kisst.cfg4j.MultiLevelProps;

public interface Module {
	public String getName();
	public void init(MultiLevelProps mlprops);
	public void reset(MultiLevelProps mlprops);
	public void destroy();
}
