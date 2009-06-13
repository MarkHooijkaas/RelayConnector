package org.kisst.cordys.http;

import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;
import org.kisst.cordys.util.NomUtil;

import com.eibus.xml.nom.Node;

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
	
	public HttpBase2(CompilationContext compiler, final int node) {
		super(compiler, node);
		headers = new HttpHeader[NomUtil.countElements(node, "header")];
		int child=Node.getFirstChild(node);
		int idx=0;
		while (child!=0) {
			if (Node.getLocalName(child).equals("header"))
				headers[idx++]=new HttpHeader(compiler, child);
			child=Node.getNextSibling(child);
		}
		urlExpression = ExpressionParser.parse(compiler, Node.getAttribute(node, "url"));
		applicationExpression  = ExpressionParser.parse(compiler, Node.getAttribute(node, "application"));
		//TODO: body=new XmlExpression(compiler, Node.getElement(node, "body"));
	}
	
	protected PostMethod createPostMethod(ExecutionContext context, HttpState state, int bodyNode) {
		HostSettings host=getHost(context);
		String url=urlExpression.getString(context);
	    PostMethod method = createPostMethod(host.url.get(props)+url, bodyNode); // TODO: handle slashes /
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
		return HttpSettings.host.get(applicationExpression.getString(context));
	}	

	protected HttpResponse call(final ExecutionContext context, int bodyNode) {
		HttpState state=createState(context);
	    PostMethod method = createPostMethod(context, state, bodyNode);
	    return httpCall(method, state);
	}
}
