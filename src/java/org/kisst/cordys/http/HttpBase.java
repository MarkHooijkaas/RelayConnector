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

package org.kisst.cordys.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.util.DnUtil;
import org.kisst.props4j.Props;

import com.eibus.xml.nom.Node;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class HttpBase {
    protected static final HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
    
    public static void reset(Props props) {
    	// TODO: better way to clear all. This will not affect active connections
    	client.getHttpConnectionManager().closeIdleConnections(0);
    	client.getParams().setAuthenticationPreemptive(true);
    	client.getHttpConnectionManager().getParams().setDefaultMaxConnectionsPerHost(HttpSettings.maxConnectionsPerHost.get(props)); 
    	client.getHttpConnectionManager().getParams().setMaxTotalConnections(HttpSettings.maxTotalConnections.get(props)); 
    }
	
    private final XmlExpression body;
    private final boolean prettyPrint;
    protected final int timeout;
    protected final Props props;
	
	public HttpBase(CompilationContext compiler, final int node) {
		props=compiler.getProps();
		prettyPrint = compiler.getSmartBooleanAttribute(node, "prettyPrint", false);
		timeout = compiler.getSmartIntAttribute(node, "timeout", HttpSettings.timeout.get(props));
		body=new XmlExpression(compiler, Node.getAttribute(node, "body", "/input/../.."));
	}

	protected int createBody(final ExecutionContext context) {
		// Note: this method always clones the XML, so it may be modified, and should always be deleted
		// It might be more efficient, to not clone, but then it becomes tricky if the node needs to be deleted
		return Node.clone(body.getNode(context), true);
	}

	protected PostMethod createPostMethod(String url, int bodyNode) {
    	String xml=Node.writeToString(bodyNode, prettyPrint);
	    PostMethod method = new PostMethod(url); // TODO: handle slashes /
	    method.getParams().setSoTimeout(timeout);
		try {
			method.setRequestEntity(new StringRequestEntity(xml, "text/xml", "UTF-8"));
		}
		catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
		return method;
	}
	
	protected HttpResponse httpCall(final PostMethod method, HttpState state, ExecutionContext context) {
    	String user=DnUtil.getFirstDnPart(context.getOrganizationalUser());
    	final Monitor mon1 = MonitorFactory.start("HttpCall:"+context.getFullMethodName());
		final Monitor mon2 = MonitorFactory.start("AllHttpCalls");
    	final Monitor monu1 = MonitorFactory.start("HttpCallForUser:"+user+":"+context.getFullMethodName());
		final Monitor monu2 = MonitorFactory.start("AllHttpCallsForUser:"+user);
		boolean success=false;
		final Date startTime=new Date();
	    try {
	    	//int statusCode = client.executeMethod(method.getHostConfiguration(), method, state);
	    	int statusCode = client.executeMethod(null, method, state);
	    	success=true;
			return new HttpResponse(statusCode, method.getResponseBody());
	    }
	    catch (HttpException e) { throw new RuntimeException(e); } 
	    catch (IOException e) {  throw new RuntimeException(e); }
	    finally {
	    	method.releaseConnection(); // TODO: what if connection not yet borrowed?
			context.getBaseConnector().logPerformance("HTTP", context, startTime, context.getRequest().getXMLNode(), success);
			mon1.stop();
			mon2.stop();
			monu1.stop();
			monu2.stop();
	    }
	}
}
