package org.kisst.cordys.as400;

import java.util.HashMap;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.as400.conn.As400Connection;
import org.kisst.cordys.as400.conn.As400ConnectionPool;
import org.kisst.cordys.as400.conn.SmartPool;
import org.kisst.cordys.as400.conn.BorrowedAs400Connection;
import org.kisst.cordys.relay.Module;
import org.kisst.cordys.script.ExecutionContext;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

public class As400Module implements Module {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(As400Module.class);
	public static final String SOAP_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";
    private static int ccsid;
    
    static public int getCcsid() { return ccsid; }

	public final As400Configuration conf=new As400Configuration();
	public static final HashMap<String, As400ConnectionPool> pools=new HashMap<String, As400ConnectionPool>();
	


	public String getName() {
		return "As400Module";
	}

	public void init(Props props) {
		createPools();
		ccsid=determineCcsid();
		logger.log(Severity.INFO, "using ccsid value: "+ccsid);
	}

	public void reset(Props props) {
		destroyPools();
		conf.reload();
		createPools();
	}

	public void destroy() {
		destroyPools();
    	logger.log(Severity.INFO, "As400Connector closed");
	}

	private void createPools() {
		pools.clear();
		String[] poolNames = conf.getPoolNames();
		for (int i=0;i<poolNames.length; i++){
			String name=poolNames[i];
			SmartPool pool = new SmartPool(conf.getPoolConfiguration(name));
			pool.init();
			logger.log(Severity.DEBUG, "aanmaken pool " + name);
			pools.put(name, pool);
		}
	}
	
	private void destroyPools() {
		String[] poolNames = conf.getPoolNames();
		for (int i=0;i<poolNames.length; i++){
			String name=poolNames[i];
			getPool(name).destroy();
		}
	}

	private int determineCcsid() {
    	if (conf.getCcsId()>=0) {
    		logger.debug("ccsid determined from configuration file");
    		return conf.getCcsId();
    	}
		boolean allCallsDone = false;
		logger.debug("Determining ccsid ousing connection");
		As400ConnectionPool mainPool = getPool("main");
    	As400Connection conn=mainPool.borrowConnection(conf.getDefaultTimeout()); //TODO moet dit aan een pool hangen
       	try {
       		int result = conn.getCcsid();
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
	
    
	public As400ConnectionPool getPool(String name) { return pools.get(name); }

	public static As400Connection getConnection(ExecutionContext context) {
		// Note: this method does not need to synchronize on the ExecutionContext object
		// because one Execution context, will only be used in one thread
		// (except fo asynchronous callbacks)
		final String key="_as400connection";
		As400Connection conn= (As400Connection) context.getCallContext().getObject(key);
		if (conn!=null)
			return conn;
		As400ConnectionPool pool = pools.get("TODO");
		conn=pool.borrowConnection(30000);
		context.getCallContext().destroyWhenDone(new BorrowedAs400Connection(pool, conn));
		context.getCallContext().setObject(key, conn);
		return conn;
	}
}
