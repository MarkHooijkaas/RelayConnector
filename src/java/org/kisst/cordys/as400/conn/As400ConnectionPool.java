package org.kisst.cordys.as400.conn;

public interface As400ConnectionPool {
    public void reset();
	public void init();
	public As400Connection borrowConnection(long timeout);	
	public void releaseConnection(As400Connection conn);
	public void invalidateConnection(As400Connection conn);
	public void destroy();
}
