package org.kisst.cordys.as400.conn;

import org.apache.commons.pool.PoolableObjectFactory;

public class As400ConnectionFactory implements PoolableObjectFactory {

	private final PoolConfiguration conf;

	public As400ConnectionFactory(PoolConfiguration conf) {
		this.conf=conf;
	}
	
	public Object makeObject() {
		return new As400Connection(conf);
	}

	public void activateObject(Object obj) {
	}

	public void passivateObject(Object obj) {
	}

	public void destroyObject(Object obj) {
		As400Connection conn= (As400Connection) obj;
		conn.close();
	}


	public boolean validateObject(Object obj) {
		return true;
	}

}
