package org.kisst.cordys.relay;

public interface Module {
	public String getName();
	public void init(RelayConnector connector);
	public void reset();
	public void destroy();
}
