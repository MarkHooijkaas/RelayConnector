package org.kisst.cordys.http;

import org.apache.commons.httpclient.methods.PostMethod;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.util.NomUtil;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.xml.nom.Node;

public class HttpCallbackStep extends HttpBase2 implements Step {
	public static final String defaultWrapperElementNamespace = "http://kisst.org/cordys/http";
	public static final String defaultWrapperElementName = "CallbackWrapper";
	
	private final String wrappperElementName;
	private final String wrappperElementNamespace;
	
	public HttpCallbackStep(CompilationContext compiler, final int node) {
		super(compiler, node);
		wrappperElementName     =compiler.getSmartAttribute(node, "wrapperName", defaultWrapperElementName);
		wrappperElementNamespace=compiler.getSmartAttribute(node, "wrapperNamespace", defaultWrapperElementNamespace);
	}
	
	public void executeStep(final ExecutionContext context) {
	    int bodyNode= createBody(context);
	    int httpResponse = 0;
	    try {
	    	int header=NomUtil.getElement(bodyNode, SoapUtil.soapNamespace, "Header");
	    	int wrapper=NomUtil.getElement(header,  wrappperElementNamespace, wrappperElementName);
	    	int address=NomUtil.getElementByLocalName(wrapper, "Address");
	    	String url=Node.getData(address);
	    	PostMethod method=createPostMethod(url, bodyNode);
		    		
	    	HttpResponse response=httpCall(method, null);
			int cordysResponse=context.getXmlVar("output");
			httpResponse = response.getResponseXml(context.getCallContext().getDocument());
			if (response.getCode()>=300) {
				if (httpResponse==0 || ! SoapUtil.isSoapFault(httpResponse))
					throw new HttpSoapFaultException(response);
			}
			if (httpResponse!=0) // This should als merge SOAP:Faults
				SoapUtil.mergeResponses(httpResponse, cordysResponse);
			// TODO: What to do if there is no XML response???
			// Note: if there is no XML response, ideally Cordys should give no XML response as well
			// However this is not possible since Cordys needs a SOAP:Header for routing purposes.
			
	    }
	    finally {
	    	Node.delete(httpResponse);
	    }
	}
}
