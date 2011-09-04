package org.kisst.cordys.connector;

import java.util.Date;


public interface PerformanceLogger {
	public void log(String type, CallContext context, Date startTime, int node, boolean status);
}
