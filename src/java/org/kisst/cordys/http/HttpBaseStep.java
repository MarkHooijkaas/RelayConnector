package org.kisst.cordys.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.util.NomUtil;

import com.eibus.xml.nom.Node;

public class HttpBaseStep {
    private static final HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
    
    public static void reset() {
    	// TODO: better way to clear all. This will not affect active connections
    	client.getHttpConnectionManager().closeIdleConnections(0);
    	client.getParams().setAuthenticationPreemptive(true);
    }
	
    private static class HttpHeader {
    	final String key;
    	final Expression value;
    	HttpHeader(CompilationContext compiler, final int node) {
    		key = Node.getAttribute(node, "key");
    		value=ExpressionParser.parse(compiler, Node.getAttribute(node, "value"));
    	}
    	
    }
    
    private final HttpConnector connector;
    private final HttpHeader headers[];
	private final Expression urlExpression;
	private final String application;
    protected final XmlExpression body;
    protected final boolean prettyPrint;
	
	public HttpBaseStep(CompilationContext compiler, final int node) {
		connector=(HttpConnector) compiler.getRelayConnector();
		headers = new HttpHeader[NomUtil.countElements(node, "header")];
		urlExpression = ExpressionParser.parse(compiler, Node.getAttribute(node, "url"));
		application = Node.getAttribute(node, "application");
		//TODO: body=new XmlExpression(compiler, Node.getElement(node, "body"));
		body=new XmlExpression(compiler, Node.getAttribute(node, "body", "/input/../.."));
		prettyPrint = compiler.getSmartBooleanAttribute(node, "prettyPrint", true);
	}

	protected PostMethod createMethod(ExecutionContext context, HttpState state, String xml) {
		HostSettings host=getHost();
		String url=urlExpression.getString(context);
	    PostMethod method = new PostMethod(host.url.get()+url); // TODO: handle slashes /
    	// TODO: better values vor mime type and encoding and make configurable
		try {
			method.setRequestEntity(new StringRequestEntity(xml, "", null));
		}
		catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
		for (HttpHeader h:headers) {
	    	method.addRequestHeader(h.key, h.value.getString(context));
		}
		if (state!=null)
			method.setDoAuthentication(true);
		return method;
	}

	protected HttpState createState() {
		HostSettings host=getHost();
		if (host.username.get() != null) {
			HttpState state=new HttpState();
			state.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(host.username.get(), host.password.get()));
			return state;
		}
		return null;
	}

	protected HostSettings getHost() {
		//connector.settings.set(connector.conf.properties);
		return connector.settings.http.host.get(application);
	}	
	
	protected byte[] retrieveResponse(final ExecutionContext context, PostMethod method, int statusCode) {
		try {
			if (statusCode >= 300 && ! connector.settings.http.ignoreReturnCode.get())
				throw new RuntimeException("Incorrect HTTP return code "+statusCode+", received message is:"+method.getResponseBody());
			return method.getResponseBody();
		}
	    catch (IOException e) {  throw new RuntimeException(e); }
	}

	protected byte[] call(final ExecutionContext context, String xml) {
		HttpState state=createState();
	    PostMethod method = createMethod(context, state, xml);
	    try {
	    	//int statusCode = client.executeMethod(method.getHostConfiguration(), method, state);
	    	int statusCode = client.executeMethod(null, method, state);
	    	return retrieveResponse(context, method, statusCode);
	    }
	    catch (HttpException e) { throw new RuntimeException(e); } 
	    catch (IOException e) {  throw new RuntimeException(e); }
	    finally {
	    	method.releaseConnection(); // TODO: what if connection not yet borrowed?
	    }
	}
}
