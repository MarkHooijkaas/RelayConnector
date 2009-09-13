package org.kisst.cordys.as400.conn;

import org.kisst.cordys.util.Destroyable;

public class BorrowedAs400Connection implements Destroyable {
	private final As400ConnectionPool pool;
	private final As400Connection     conn;
	
	public BorrowedAs400Connection(As400ConnectionPool pool, As400Connection conn) {
		this.conn=conn;
		this.pool=pool;
	}
	public void destroy() {
		pool.releaseConnection(conn);
	}
	
}