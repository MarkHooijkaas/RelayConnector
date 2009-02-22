package org.kisst.cordys.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.expression.XmlExpression;

import com.eibus.xml.nom.Node;

public class HttpBase {
    private static final HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
    
    public static void reset() {
    	// TODO: better way to clear all. This will not affect active connections
    	client.getHttpConnectionManager().closeIdleConnections(0);
    	client.getParams().setAuthenticationPreemptive(true);
    }
	
    private final HttpConnector connector;
    private final XmlExpression body;
    private final boolean prettyPrint;
	
	public HttpBase(CompilationContext compiler, final int node) {
		connector=(HttpConnector) compiler.getRelayConnector();
		prettyPrint = compiler.getSmartBooleanAttribute(node, "prettyPrint", true);
		body=new XmlExpression(compiler, Node.getAttribute(node, "body", "/input/../.."));
	}

	protected HttpConnector.Settings getSettings() {
		return connector.settings;
	}

	protected int createBody(final ExecutionContext context) {
		// Note: this method always clones the XML, so it may be modified, and should always be deleted
		// It might be more efficient, to not clone, but then it becomes tricky if the node needs to be deleted
		return Node.clone(body.getNode(context), true);
	}

	protected PostMethod createPostMethod(String url, int bodyNode) {
    	String xml=Node.writeToString(bodyNode, prettyPrint);
	    PostMethod method = new PostMethod(url); // TODO: handle slashes /
		try {
			method.setRequestEntity(new StringRequestEntity(xml, "text/xml", "UTF-8"));
		}
		catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
		return method;
	}
	
	private String retrieveResponse(PostMethod method, int statusCode) {
		try {
			String  result=new String(method.getResponseBody(), "utf-8"); // TODO: use correct encoding
			if (statusCode >= 300 && ! connector.settings.http.ignoreReturnCode.get()) {
				throw new HttpSoapFaultException(statusCode, result);
			}
			return result; 
		}
	    catch (IOException e) {  throw new RuntimeException(e); }
	}

	protected String httpCall(final PostMethod method, HttpState state) {
    	if (connector.settings.http.wireLogging.get()!=null)
    		setLogger("httpclient.wire", connector.settings.http.wireLogging.get());
	    try {
	    	//int statusCode = client.executeMethod(method.getHostConfiguration(), method, state);
	    	int statusCode = client.executeMethod(null, method, state);
	    	return retrieveResponse(method, statusCode);
	    }
	    catch (HttpException e) { throw new RuntimeException(e); } 
	    catch (IOException e) {  throw new RuntimeException(e); }
	    finally {
	    	method.releaseConnection(); // TODO: what if connection not yet borrowed?
	    }
	}

	
	private void setLogger(String loggerName, String levelName) {
		try {
			// dirty trick, use reflection, so no dependency is on log4j libraries
			// this will prevent linkage errors if log4j is not present.
			Object level = Class.forName("org.apache.log4j.Level").getField(levelName).get(null);
			Object logger = Class.forName("org.apache.log4j.Logger").getMethod("getLogger", new Class[] {String.class} ).invoke(null, new Object[] {loggerName});
			logger.getClass().getMethod("setLevel", new Class[] { level.getClass()}).invoke(logger, new Object[] { level });
		} catch (Exception e) { throw new RuntimeException(e); /* ignore, log4j is not working */		}
	}
}
