package org.kisst.cordys.connector;

import java.util.Date;

import org.kisst.cordys.script.ExecutionContext;

public interface PerformanceLogger {
	public void log(String type, ExecutionContext context, Date startTime, int node, boolean status);
}
