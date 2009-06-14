package org.kisst.cordys.relay;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

public class RelayTimer {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(RelayTimer.class);
	
	private final long start=System.currentTimeMillis();

	public void log(String msg) {
		logger.log(Severity.INFO, (System.currentTimeMillis()-start)+msg);
	}

}
