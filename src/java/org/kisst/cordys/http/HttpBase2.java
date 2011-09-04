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

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.kisst.cordys.relay.RelaySettings;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;
import org.kisst.cordys.util.JamonUtil;
import org.kisst.cordys.util.NomUtil;

import com.eibus.xml.nom.Node;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class HttpBase2 extends HttpBase {
    private static class HttpHeader {
    	final String key;
    	final Expression value;
    	HttpHeader(CompilationContext compiler, final int node) {
    		key = Node.getAttribute(node, "key");
    		value=ExpressionParser.parse(compiler, Node.getAttribute(node, "value"));
    	}
    	
    }
    
    private final HttpHeader headers[];
	private final Expression urlExpression;
	private final Expression applicationExpression ;
	private final boolean doPost;
	
	public HttpBase2(CompilationContext compiler, final int node) {
		super(compiler, node);
		headers = new HttpHeader[NomUtil.countElements(node, "header")];
		int child=Node.getFirstElement(node);
		int idx=0;
		while (child!=0) {
			if (Node.getLocalName(child).equals("header"))
				headers[idx++]=new HttpHeader(compiler, child);
			child=Node.getNextSibling(child);
		}
		doPost="POST".equals(Node.getAttribute(node, "method", "POST"));
		urlExpression = ExpressionParser.parse(compiler, Node.getAttribute(node, "url"));
		applicationExpression  = ExpressionParser.parse(compiler, Node.getAttribute(node, "application"));
		//TODO: body=new XmlExpression(compiler, Node.getElement(node, "body"));
	}
	
	protected PostMethod createPostMethod(ExecutionContext context, HttpState state, int bodyNode) {
		HostSettings host=getHost(context);
		String url=urlExpression.getString(context);
		String urlstart=host.url.get(props);
		if (urlstart==null || urlstart.trim().length()==0)
			throw new RuntimeException("Could not find http configuration "+applicationExpression.getString(context));
	    PostMethod method = createPostMethod(urlstart+url, bodyNode); // TODO: handle slashes /
		for (HttpHeader h:headers) {
	    	method.addRequestHeader(h.key, h.value.getString(context));
		}
		if (state!=null)
			method.setDoAuthentication(true);
		return method;
	}

	protected GetMethod createGetMethod(ExecutionContext context, HttpState state) {
		HostSettings host=getHost(context);
		String url=urlExpression.getString(context);
		String urlstart=host.url.get(props);
		if (urlstart==null || urlstart.trim().length()==0)
			throw new RuntimeException("Could not find http configuration "+applicationExpression.getString(context));
	    GetMethod method = new GetMethod(urlstart+url); // TODO: handle slashes /
	    method.getParams().setSoTimeout(timeout);

		for (HttpHeader h:headers) {
	    	method.addRequestHeader(h.key, h.value.getString(context));
		}
		if (state!=null)
			method.setDoAuthentication(true);
		return method;
	}

	
	protected HttpState createState(final ExecutionContext context) {
		HostSettings host=getHost(context);
		if (host.username.get(props) != null) {
			HttpState state=new HttpState();
			state.setCredentials(AuthScope.ANY, host.getCredentials(props));
			return state;
		}
		return null;
	}

	protected HostSettings getHost(final ExecutionContext context) {
		//connector.settings.set(connector.conf.properties);
		String key=applicationExpression.getString(context);
		HostSettings result = HttpSettings.host.get(key);
		return result;
	}	

	protected HttpResponse call(final ExecutionContext context, int bodyNode) {
    	if (context.infoTraceEnabled()) {
    		int reqnode=bodyNode;
    		if (RelaySettings.traceShowEnvelope.get(props))
    			reqnode=NomUtil.getRootNode(bodyNode);
    		context.traceInfo("Sending HTTP request: ",reqnode);
    	}
		HttpState state=createState(context);
		HttpResponse result;
	    if (doPost) {
	    	PostMethod method = createPostMethod(context, state, bodyNode);
	    	result=httpCall(method, state, context);
	    }
	    else {
	    	GetMethod method = createGetMethod(context, state);
	    	String user=JamonUtil.getFirstDnPart(context.getOrganizationalUser());
	    	final Monitor mon1 = MonitorFactory.start("HttpCall:"+context.getFullMethodName());
			final Monitor mon2 = MonitorFactory.start("AllHttpCalls");
	    	final Monitor monu1 = MonitorFactory.start("HttpCallForUser:"+user+":"+context.getFullMethodName());
			final Monitor monu2 = MonitorFactory.start("AllHttpCallsForUser:"+user);
		    try {
		    	//int statusCode = client.executeMethod(method.getHostConfiguration(), method, state);
		    	int statusCode = client.executeMethod(null, method, state);
				return new HttpResponse(statusCode, method.getResponseBody());
		    }
		    catch (HttpException e) { throw new RuntimeException(e); } 
		    catch (IOException e) {  throw new RuntimeException(e); }
		    finally {
		    	method.releaseConnection(); // TODO: what if connection not yet borrowed?
				mon1.stop();
				mon2.stop();
				monu1.stop();
				monu2.stop();
		    }
	    }	    
    	if (context.infoTraceEnabled()) {
    		try {
    			context.traceInfo("Received HTTP response: "+result.getCode()+" "+result.getResponseString());
    		}
    		catch(Exception e) {
    			// the getResponseString could throw a wrapped UnsupportedEncoding exception
    			// to be safe I catch all errors, because tracing should not break the function 
    			context.traceInfo("Could not log HTTP response, due to following error: "+e.getMessage());
    		}
    	}
	    return result;
	}
}
