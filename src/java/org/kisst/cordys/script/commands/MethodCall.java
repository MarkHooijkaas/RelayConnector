package org.kisst.cordys.script.commands;

import java.util.Date;

import org.kisst.cordys.relay.SoapFaultException;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.script.xml.ElementAppender;
import org.kisst.cordys.util.NomUtil;

import com.eibus.connector.nom.Connector;
import com.eibus.connector.nom.SOAPMessageListener;
import com.eibus.directory.soap.DirectoryException;
import com.eibus.exception.ExceptionGroup;
import com.eibus.exception.TimeoutException;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class MethodCall {
	private static final CordysLogger logger=CordysLogger.getCordysLogger(MethodCall.class);
	private static final String SOAP_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";
	
	private final String namespace;
	private final String methodName;
	private final boolean async;
	private final boolean showSoap;
	private final boolean ignoreSoapFault;
	private final ElementAppender appender;
	private final XmlExpression appendMessagesTo;
	//private final XmlExpression appendSoapHeadersFrom;
	//private final XmlExpression appendSoapHeader;
	private final String resultVar;
	private final long timeout;
	
	public MethodCall(CompilationContext compiler, final int node) {
		this(compiler, node, Node.getAttribute(node, "method"));
	}
	
	public MethodCall(CompilationContext compiler, final int node, String defaultResultVar) {
		methodName=Node.getAttribute(node, "method");
		if (methodName==null)
			throw new RuntimeException("attribute method should be set");

		namespace=compiler.getSmartAttribute(node, "namespace", null);
		if (namespace==null)
			throw new RuntimeException("attribute namespace should be set or a default should be defined");

		async=compiler.getSmartBooleanAttribute(node, "async", false);
		showSoap=compiler.getSmartBooleanAttribute(node, "showSoap", false);
		ignoreSoapFault=compiler.getSmartBooleanAttribute(node, "ignoreSoapFault", false);
		appender=new ElementAppender(compiler, node);
		String appendMessagesToString = compiler.getSmartAttribute(node, "appendMessagesTo", null);
		if (appendMessagesToString==null)
			appendMessagesTo=null;
		else
			appendMessagesTo= new XmlExpression(compiler, appendMessagesToString);
/*
		String appendSoapHeadersFromStr = compiler.getSmartAttribute(node, "appendSoapHeadersFrom", null);
		if (appendSoapHeadersFromStr==null)
			appendSoapHeadersFrom=null;
		else
			appendSoapHeadersFrom= new XmlExpression(compiler, appendSoapHeadersFromStr);

		String appendSoapHeaderStr = compiler.getSmartAttribute(node, "appendSoapHeadersFrom", null);
		if (appendSoapHeaderStr==null)
			appendSoapHeader=null;
		else
			appendSoapHeader= new XmlExpression(compiler, appendSoapHeadersFromStr);
*/
		
		String tmpName=Node.getAttribute(node, "resultVar");
		resultVar = tmpName==null? defaultResultVar : tmpName;
		compiler.declareXmlVar(resultVar);
		String timeoutString=Node.getAttribute(node, "timeout");
		if (timeoutString==null)
			timeout=compiler.getConfiguration().getTimeout();
		else
			timeout=Long.parseLong(timeoutString);
	}
	
	protected int createMethod(final ExecutionContext context) {
		String effectiveNamespace = namespace;
		if (effectiveNamespace == null )
			effectiveNamespace = context.getTextVar("namespace");
		String dnUser=context.getOrganizationalUser();
		String dnOrganization=context.getOrganization();
		Connector connector = context.getRelayConnector().getConnector();
		int method;
		try {
			method = connector.createSOAPMethod(dnUser, dnOrganization, effectiveNamespace, methodName);
		} 
		catch (DirectoryException e) {  throw new RuntimeException("Error when handling method "+methodName,e); }
		appender.append(context, method);
		return method;

	}

	protected void callMethod(final ExecutionContext context, int method) {
		try {
			if (appendMessagesTo!=null) {
				int logNode=appendMessagesTo.getNode(context);
				Node.duplicateAndAppendToChildren(method, method, logNode);
			}
			if (logger.isInfoEnabled())
				logger.log(Severity.INFO, "sending request\n"+Node.writeToString(method, true));
			Connector connector = context.getRelayConnector().getConnector();
			if (async) {
				context.createXmlSlot(resultVar, methodName, new Date().getTime()+timeout);
				connector.sendAndCallback(Node.getParent(method),new SOAPMessageListener() {
					public boolean onReceive(int message)
					{
						handleResponse(context, message);
						return false; // Node should not yet be destroyed by Callback caller!!
					}
				});
			}
			else {
				handleResponse(context, connector.sendAndWait(Node.getParent(method),timeout));
			}
		}
		catch (TimeoutException e) { throw new RuntimeException("Timeout when calling method "+methodName,e); }
		catch (ExceptionGroup e) { throw new RuntimeException("Error when calling method "+methodName,e); }
	}

	private void handleResponse(ExecutionContext context, int response) {
		if (logger.isInfoEnabled())
			logger.log(Severity.INFO, "received response\n"+Node.writeToString(response, true));
		if (appendMessagesTo!=null) {
			int logNode=appendMessagesTo.getNode(context);
			Node.duplicateAndAppendToChildren(response, response, logNode);
		}
		int responseBody=stripSoap(response);
		if (! ignoreSoapFault) {
			if ("Fault".equals(Node.getLocalName(responseBody)) 
					&& SOAP_NAMESPACE.equals(Node.getNamespaceURI(responseBody))) 
			{
				int codeNode=NomUtil.getElement(responseBody, SOAP_NAMESPACE, "faultcode");
				String code=Node.getData(codeNode);
				int messageNode=NomUtil.getElement(responseBody, SOAP_NAMESPACE, "faultstring");
				String message=Node.getData(messageNode);
				// TODO: handle details, actor and other fields
				if (async)
					context.setAsynchronousError(new SoapFaultException(code, message));
				else
					throw new SoapFaultException(code, message);
			}
		}
		if (showSoap)
			context.setXmlVar(resultVar, response);
		else
			context.setXmlVar(resultVar, responseBody);
	}
	
	private int stripSoap(int node) {
		if (showSoap)
			return node;
		node=NomUtil.getElement(node, SOAP_NAMESPACE, "Body");
		node=Node.getFirstChild(node);  // get response node
		return node;
	}

	public String getMethodName() {	return methodName;	}
	
}
