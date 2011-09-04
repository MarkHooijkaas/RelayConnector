package org.kisst.cordys.connector;

import org.kisst.cordys.script.ExecutionContext;

public interface XmlInterceptor {
	public void intercept(ExecutionContext context, int node);
}
