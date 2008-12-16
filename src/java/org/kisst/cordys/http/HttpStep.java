package org.kisst.cordys.http;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.util.NomUtil;

import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

public class HttpStep implements Step {
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
    
    private final HttpHeader headers[];
	private final Expression urlExpression;
	private final String application;
    private final XmlExpression body;
    private final String resultVar;
	private final boolean prettyPrint;
	
	public HttpStep(CompilationContext compiler, final int node) {
		headers = new HttpHeader[NomUtil.countElements(node, "header")];
		urlExpression = ExpressionParser.parse(compiler, Node.getAttribute(node, "url"));
		application = Node.getAttribute(node, "application");
		//TODO: body=new XmlExpression(compiler, Node.getElement(node, "body"));
		body=new XmlExpression(compiler, Node.getAttribute(node, "body"));
		resultVar = Node.getAttribute(node, "resultVar", application);
		compiler.declareXmlVar(resultVar);
		prettyPrint = compiler.getSmartBooleanAttribute(node, "prettyPrint", true);
	}
	
	public void executeStep(final ExecutionContext context) {
	    int bodyNode= body.getNode(context);
		String url=urlExpression.getString(context);
		HttpConnector connector = (HttpConnector) context.getRelayConnector();
		connector.settings.set(connector.conf.properties);
		HostSettings host=connector.settings.http.host.get(application);
	    PostMethod method = new PostMethod(host.url.get()+url); // TODO: handle slashes /
	    //PostMethod method = new PostMethod("http://10.10.10.103/test/"+url); // TODO: handle slashes /
		HttpState state=null;
	    if (host.username.get() != null) {
	    	method.setDoAuthentication(true);
	    	state=new HttpState();
			state.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(host.username.get(), host.password.get()));
	    }
	    String xml=Node.writeToString(bodyNode, prettyPrint);
	    try {
	    	// TODO: better values vor mime type and encoding and make configurable
			method.setRequestEntity(new StringRequestEntity(xml, "", null));
			for (HttpHeader h:headers) {
		    	method.addRequestHeader(h.key, h.value.getString(context));
			}
	    	//int statusCode = client.executeMethod(method.getHostConfiguration(), method, state);
	    	int statusCode = client.executeMethod(null, method, state);
	    	if (statusCode != HttpStatus.SC_OK) {
	    		throw new RuntimeException("Incorrect HTTP return code "+statusCode);
	    	}
	    	byte[] responseBytes = method.getResponseBody();
	    	int responseNode = context.getDocument().load(responseBytes);
	    	context.setXmlVar(resultVar, responseNode);
	    }
	    catch (HttpException e) { throw new RuntimeException(e); } 
	    catch (IOException e) {  throw new RuntimeException(e); }
	    catch (XMLException e) { throw new RuntimeException(e); }
	    finally {
	    	method.releaseConnection(); // TODO: what if connection not yet borrowed?
	    }
	}
}
