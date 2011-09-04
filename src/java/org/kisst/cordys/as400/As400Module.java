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

import java.util.HashMap;

import org.kisst.cordys.as400.conn.As400Connection;
import org.kisst.cordys.as400.conn.As400ConnectionPool;
import org.kisst.cordys.as400.conn.BorrowedAs400Connection;
import org.kisst.cordys.connector.BaseConnector;
import org.kisst.cordys.connector.Module;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.GenericCommand;
import org.kisst.cordys.script.commands.CommandList;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

public class As400Module implements Module {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(As400Module.class);
	public static final String SOAP_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";
    private BaseConnector connector; // TODO: make final, needs change in module loading
    

	public static final HashMap<String, As400ConnectionPool> pools=new HashMap<String, As400ConnectionPool>();
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
		pools.clear();
		String[] poolNames = new String[] {"main"};
		for (int i=0;i<poolNames.length; i++){
			String name=poolNames[i];
			As400ConnectionPool pool = new As400ConnectionPool(As400Settings.host.get(name), connector.getGlobalProps() );
			pool.init();
			logger.log(Severity.DEBUG, "aanmaken pool " + name);
			pools.put(name, pool);
		}
		ccsid = determineCcsid();
	}
	
	private void destroyPools() {
		String[] poolNames = new String[] {"main"};
		for (int i=0;i<poolNames.length; i++){
			String name=poolNames[i];
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
		As400ConnectionPool pool = pools.get("main");
		conn=pool.borrowConnection(context.getProps());
		context.destroyWhenDone(new BorrowedAs400Connection(pool, conn));
		context.setObject(key, conn);
		return conn;
	}
	
	private int determineCcsid() {
		int result = As400Settings.ccsid.get(connector.getGlobalProps());

    	if (result>=0) {
    		logger.debug("ccsid determined from configuration file");
    		return result;
    	}
		boolean allCallsDone = false;
		logger.debug("Determining ccsid ousing connection");
		As400ConnectionPool mainPool = getPool("main");
    	As400Connection conn=mainPool.borrowConnection(connector.getGlobalProps());
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
