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
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.LongSetting;
import org.kisst.cfg4j.MappedSetting;
import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.StringSetting;
import org.kisst.cordys.http.HostSettings;
import org.kisst.cordys.http.HttpResponse;
import org.kisst.cordys.http.HttpSoapFaultException;
import org.kisst.cordys.relay.RelayTrace;
import org.kisst.cordys.relay.RelayedSoapFaultException;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class EsbTransaction  implements ApplicationTransaction {
	private static final MultiThreadedHttpConnectionManager connmngr = new MultiThreadedHttpConnectionManager();
	private static final HttpClient client = new HttpClient(connmngr);

	private final static CompositeSetting esb=new CompositeSetting(null,"esb");
	//private static final HostSettings host=new HostSettings(null,"esb");
	private final static StringSetting hosts=new StringSetting(esb, "hosts", null);
	private final static MappedSetting<HostSettings> host=new MappedSetting<HostSettings>(esb, "host", HostSettings.class);;
	private final static LongSetting closeIdleConnections=new LongSetting(esb, "closeIdleConnections", -1);

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
    	catch (RelayedSoapFaultException e) {
    		e.createResponse(response);
    	}
    	catch (Exception e) {
    		RelayTrace.logger.log(Severity.ERROR, "TECHERR.ESB", e);
    		response.createSOAPFault("TECHERR.ESB",e.toString());
    	}
        return true; // connector has to send the response
    }

    
	private void exec(int input, int output) {
		long l=closeIdleConnections.get(props);
		if (l>=0) // Hack because often some idle connections were closed which resulted in 401 errors
			connmngr.closeIdleConnections(l);
		
		String[] hostnames = hosts.get(props).split(",");
		int bodyNode= Node.getParent(Node.getParent(input));
		for (int i=0; i<hostnames.length; i++) {
			String hostname=hostnames[i].trim();
			PostMethod method = createPostMethod(host.get(hostname).url.get(props), bodyNode);
			HttpState state=createState(hostname);
			if (state!=null)
				method.setDoAuthentication(true);
			int httpResponse = 0;
			try {
				HttpResponse response= httpCall(method, state);
				int cordysResponse=output; //Node.getParent(Node.getParent(output));;
				httpResponse = response.getResponseXml(Node.getDocument(input));
				SoapUtil.mergeResponses(httpResponse, cordysResponse);
				return;
			}
			catch(RuntimeException e) {
				if (i<hostnames.length-1)
					RelayTrace.logger.log(Severity.WARN, "Error trying to call host "+hostname+" trying next host "+hostnames[i+1],e);
				else
					throw e;
			}
			finally {
				if (httpResponse!=0) Node.delete(httpResponse);
			}
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

	private HttpState createState(String hostname) {
		if (host.get(hostname).username.get(props) == null)
			return null;
		HttpState state=new HttpState();
		state.setCredentials(AuthScope.ANY, host.get(hostname).getCredentials(props));
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
