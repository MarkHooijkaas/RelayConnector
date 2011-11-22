/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.kisst.cordys.as400;

import java.util.LinkedHashMap;

import org.kisst.cordys.as400.conn.As400Connection;
import org.kisst.cordys.as400.conn.As400ConnectionPool;
import org.kisst.cordys.as400.conn.BorrowedAs400Connection;
import org.kisst.cordys.connector.BaseConnector;
import org.kisst.cordys.connector.Module;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.GenericCommand;
import org.kisst.cordys.script.commands.CommandList;
import org.kisst.props4j.Props;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

public class As400Module implements Module {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(As400Module.class);

	public static final String AS400_POOL_NAME_KEY = "_as400PoolName";
	public static final String SOAP_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";
    private BaseConnector connector; // TODO: make final, needs change in module loading
    

	public static final LinkedHashMap<String, As400ConnectionPool> pools=new LinkedHashMap<String, As400ConnectionPool>();
    private static int ccsid;
    static public int getCcsid() { return ccsid; }

	public String getName() {
		return "As400Module";
	}

	public void init(BaseConnector connector) {
		this.connector = connector;
		createPools();
    	CommandList.addBasicCommand("as400prog",  new GenericCommand(ProgramStep.class));
    	CommandList.addBasicCommand("as400cmd",  new GenericCommand(CommandStep.class));
	}

	public void reset() {
		destroyPools();
		createPools();
	}

	public void destroy() {
		destroyPools();
    	logger.log(Severity.INFO, "As400Connector closed");
	}

	private void createPools() {
		Props props = connector.getProps();
		pools.clear();
		for (String name: As400Settings.pools.keys(props)){
			As400ConnectionPool pool = new As400ConnectionPool(As400Settings.pools.get(name), props );
			pool.init();
			logger.log(Severity.DEBUG, "aanmaken pool " + name);
			pools.put(name, pool);
		}
		ccsid = determineCcsid();
	}
	
	private void destroyPools() {
		Props props = connector.getProps();
		for (String name: As400Settings.pools.keys(props)){
			getPool(name).destroy();
		}
	}

	public As400ConnectionPool getPool(String name) { return pools.get(name); }

	public static As400Connection getConnection(ExecutionContext context) {
		// Note: this method does not need to synchronize on the ExecutionContext object
		// because one Execution context, will only be used in one thread
		// (except fo asynchronous callbacks)
		final String key="_as400connection";
		As400Connection conn= (As400Connection) context.getObject(key);
		if (conn!=null)
			return conn;
		String poolName = context.getTextVar(AS400_POOL_NAME_KEY);
		As400ConnectionPool pool = null;
		if (poolName==null) {
			if (pools.size()==1)
				pool=getFirstPool();
			else
				throw new RuntimeException("Could not determine correct As400Connection pool and multiple pools exist");
		}
		else
			pool = pools.get(poolName);
		conn=pool.borrowConnection(context.getProps());
		context.destroyWhenDone(new BorrowedAs400Connection(pool, conn));
		context.setObject(key, conn);
		return conn;
	}
	
	private static As400ConnectionPool getFirstPool() {
		for (As400ConnectionPool pool : pools.values())
			return pool;
		throw new RuntimeException("no connection pools defined");
	}
	private int determineCcsid() {
		int result = As400Settings.ccsid.get(connector.getProps());

    	if (result>=0) {
    		logger.debug("ccsid determined from configuration file");
    		return result;
    	}
		boolean allCallsDone = false;
		logger.debug("Determining ccsid ousing connection");
		
		As400ConnectionPool mainPool = getFirstPool();
    	As400Connection conn=mainPool.borrowConnection(connector.getProps());
       	try {
       		result = conn.getCcsid();
			allCallsDone = true;
			return result;
		}
       	finally {
			if (allCallsDone)
				mainPool.releaseConnection(conn);
			else
				mainPool.invalidateConnection(conn);
       	}
	}

}
