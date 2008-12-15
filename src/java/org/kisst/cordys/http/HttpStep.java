package org.kisst.cordys.http;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
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
		application = Node.getAttribute(node, "application ");
		//TODO: body=new XmlExpression(compiler, Node.getElement(node, "body"));
		body=new XmlExpression(compiler, Node.getAttribute(node, "url"));
		resultVar = Node.getAttribute(node, "resultVar");
		prettyPrint = compiler.getSmartBooleanAttribute(node, "prettyPrint", true);
	}
	
	public void executeStep(final ExecutionContext context) {
	    int bodyNode= body.getNode(context);
		String url=urlExpression.getString(context);
		HttpConnector connector = (HttpConnector) context.getRelayConnector();
		HostSettings host=connector.settings.http.getHost(application);
	    PostMethod method = new PostMethod(url);
	    String xml=Node.writeToString(bodyNode, prettyPrint);
	    try {
	    	// TODO: better values vor mime type and encoding and make configurable
			method.setRequestEntity(new StringRequestEntity(xml, "", null));
			for (HttpHeader h:headers) {
		    	method.addRequestHeader(h.key, h.value.getString(context));
			}
			byte[] responseBytes;
	    	int statusCode = client.executeMethod(method);
	    	if (statusCode != HttpStatus.SC_OK) {
	    		throw new RuntimeException("Incorrect HTTP return code "+statusCode);
	    	}
	    	responseBytes = method.getResponseBody();
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
