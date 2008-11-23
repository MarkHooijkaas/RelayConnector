package org.kisst.cordys.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
import org.kisst.cordys.script.xml.ElementAppender;
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
    	private final String key;
    	private final Expression value;
    	HttpHeader(CompilationContext compiler, final int node) {
    		key = Node.getAttribute(node, "key");
    		value=ExpressionParser.parse(compiler, Node.getAttribute(node, "value"));
    	}
    	
    }
    
    private final HttpHeader headers[];
	private final Expression urlExpression;
	private final ElementAppender body;
	private final String resultVar;
	private final boolean prettyPrint;
	
	public HttpStep(CompilationContext compiler, final int node) {
		headers = new HttpHeader[NomUtil.countElements(node, "header")];
		urlExpression = ExpressionParser.parse(compiler, Node.getAttribute(node, "url"));
		body=new ElementAppender(compiler, Node.getElement(node, "body"));
		resultVar = Node.getAttribute(node, "resultVar");
		prettyPrint = compiler.getSmartBooleanAttribute(node, "prettyPrint", true);
	}
	
	public void executeStep(final ExecutionContext context) {
	    int envelopeNode=context.getDocument().createElement("SOAP:Envelope");
	    Node.setAttribute(envelopeNode, "xmlns:SOAP", "http://schemas.xmlsoap.org/soap/envelope/");
	    //TODO: int headerNode=Node.createElement("SOAP:Header", envelopeNode);
	    int bodyNode=Node.createElement("SOAP:Body", envelopeNode);
	    body.append(context, bodyNode);
		String url=urlExpression.getString(context);
	    PostMethod method = new PostMethod(url);
	    String xml=Node.writeToString(envelopeNode, prettyPrint);
	    try {
	    	// TODO: better values vor mime type and encoding and make configurable
			method.setRequestEntity(new StringRequestEntity(xml, "", null));
		}
	    catch (UnsupportedEncodingException e) { throw new RuntimeException("encoding problem",e);} 
	    
	    byte[] responseBytes;
	    try {
	    	int statusCode = client.executeMethod(method);

	    	if (statusCode != HttpStatus.SC_OK) {
	    		throw new RuntimeException("Incorrect HTTP return code "+statusCode);
	    	}
	    	responseBytes = method.getResponseBody();
	    } 
	    catch (HttpException e) { throw new RuntimeException("HttpError",e); } 
	    catch (IOException e) {  throw new RuntimeException("IoError",e); }
	    finally {
	    	method.releaseConnection();
	    }
	    try {
	    	int responseNode = context.getDocument().load(responseBytes);
	    	context.setXmlVar(resultVar, responseNode);
	    }
	    catch (XMLException e) { throw new RuntimeException("xml parsing error ", e); }
	}
}
