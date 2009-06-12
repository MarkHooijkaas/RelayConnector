package org.kisst.cordys.esb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.kisst.cfg4j.Props;
import org.kisst.cordys.http.HostSettings;
import org.kisst.cordys.http.HttpResponse;
import org.kisst.cordys.http.HttpSoapFaultException;
import org.kisst.cordys.relay.SoapFaultException;
import org.kisst.cordys.script.RelayTrace;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class EsbTransaction  implements ApplicationTransaction {
	private static final HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
	private static final HostSettings host=new HostSettings(null,"esb");

	private final Props props;
	
	public EsbTransaction(Props props) {
		this.props=props;
	}

    public boolean canProcess(String callType) {
   		return true; // Always return true!!!!
    }
    
    public void commit() {}
    public void abort() {}

    public boolean process(BodyBlock request, BodyBlock response) {
    	try {
    		exec(request.getXMLNode(), response.getXMLNode());
    	}
    	catch (SoapFaultException e) {
    		e.createResponse(response, props);
    	}
    	catch (Exception e) {
    		RelayTrace.logger.log(Severity.ERROR, "Error", e);
    		response.createSOAPFault("UnknownError",e.toString());
    	}
        return true; // connector has to send the response
    }

    
	private void exec(int input, int output) {
		int httpResponse = 0;
		try {
			int bodyNode= Node.getParent(Node.getParent(input));
		    PostMethod method = createPostMethod(host.url.get(props), bodyNode);
			HttpState state=createState();
		    if (state!=null)
		    	method.setDoAuthentication(true);
		    HttpResponse response= httpCall(method, state);
			int cordysResponse=output; //Node.getParent(Node.getParent(output));;
			httpResponse = response.getResponseXml(Node.getDocument(input));
			SoapUtil.mergeResponses(httpResponse, cordysResponse);
		}
		finally {
			if (httpResponse!=0) Node.delete(httpResponse);
		}
	}
	
	private PostMethod createPostMethod(String url, int bodyNode) {
    	String xml=Node.writeToString(bodyNode, false);
	    PostMethod method = new PostMethod(url);
	    method.getParams().setSoTimeout(props.getInt("timeout", 30000));
		try {
			method.setRequestEntity(new StringRequestEntity(xml, "text/xml", "UTF-8"));
		}
		catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
	    return method;
	}

	private HttpState createState() {
		if (host.username.get(props) == null)
			return null;
		HttpState state=new HttpState();
		state.setCredentials(AuthScope.ANY, host.getCredentials(props));
		return state;
	}

	private HttpResponse httpCall(final PostMethod method, HttpState state) {
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

	private HttpResponse retrieveResponse(PostMethod method, int statusCode) {
		try {
			HttpResponse result=new HttpResponse(statusCode, method.getResponseBody());
			if (statusCode >= 300) {
				throw new HttpSoapFaultException(result);
			}
			return result;
		}
	    catch (IOException e) {  throw new RuntimeException(e); }
	}
}
