package org.kisst.cordys.script.commands;

import java.util.Date;

import org.kisst.cordys.relay.SoapFaultException;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.script.xml.ElementAppender;
import org.kisst.cordys.util.SoapUtil;

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
	
	private final String namespace;
	private final String methodName;
	private final Expression methodExpression;
	private final boolean async;
	private final boolean showSoap;
	private final boolean ignoreSoapFault;
	private final Severity logSoapFault;
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
		String expr=Node.getAttribute(node, "methodExpression");
		if (methodName==null && expr==null)
			throw new RuntimeException("attribute method or methodExpression should be set");
		if (methodName!=null && expr!=null)
			throw new RuntimeException("attribute method and methodExpression should not be set both");
		if (expr!=null)
			methodExpression=ExpressionParser.parse(compiler,expr);
		else
			methodExpression=null;
		
		namespace=compiler.getSmartAttribute(node, "namespace", null);
		if (namespace==null)
			throw new RuntimeException("attribute namespace should be set or a default should be defined");

		async=compiler.getSmartBooleanAttribute(node, "async", false);
		showSoap=compiler.getSmartBooleanAttribute(node, "showSoap", false);
		ignoreSoapFault=compiler.getSmartBooleanAttribute(node, "ignoreSoapFault", false);
		logSoapFault=parseSeverity(compiler.getSmartAttribute(node, "logSoapFault", compiler.getConfiguration().settings.logSoapFaults.get()));
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
		if (resultVar==null)
			throw new RuntimeException("resultVar should be defined when using methodExpression");
		compiler.declareXmlVar(resultVar);
		String timeoutString=Node.getAttribute(node, "timeout");
		if (timeoutString==null)
			timeout=compiler.getConfiguration().getTimeout();
		else
			timeout=Long.parseLong(timeoutString);
	}
	
	private Severity parseSeverity(String sev) {
		if (sev==null)           return null;
		if (sev.equals("NONE"))  return null;
		if (sev.equals("DEBUG")) return Severity.DEBUG;
		if (sev.equals("INFO"))  return Severity.INFO;
		if (sev.equals("WARN"))  return Severity.WARN;
		if (sev.equals("ERROR")) return Severity.ERROR;
		if (sev.equals("FATAL")) return Severity.FATAL;
		throw new RuntimeException("unknown LogLevel ["+sev+"] should be NONE, DEBUG, INFO, WARN, ERROR or FATAL");
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
			String m=methodName;
			if (m==null)
				m=methodExpression.getString(context);
			method = connector.createSOAPMethod(dnUser, dnOrganization, effectiveNamespace, m);
		} 
		catch (DirectoryException e) {  throw new RuntimeException("Error when handling method "+methodName,e); }
		appender.append(context, method);
		return method;

	}

	// TODO: fix possible memory leak if async calls never return a answer
	// Such call will register a SOAPListener which will never be removed.
	// In long term this could lead to a OutOfMemory error.
	// Possible fix would be to not use sendAndCallback, but just send, and use a 
	// default SOAPListener. This in considered not very urgent yet.
	
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
		boolean ok=false;
		try {
			if (logger.isInfoEnabled())
				logger.log(Severity.INFO, "received response\n"+Node.writeToString(response, true));
			if (appendMessagesTo!=null) {
				int logNode=appendMessagesTo.getNode(context);
				Node.duplicateAndAppendToChildren(response, response, logNode);
			}
			int responseBody=SoapUtil.getContent(response);
			if (SoapUtil.isSoapFault(responseBody)) {
				if (logSoapFault!=null)
					logger.log(logSoapFault, "Calling method "+methodName+" returned Fault: "+Node.writeToString(responseBody, true));
				if (! ignoreSoapFault) {
					if (async) {
						context.setAsynchronousError(new SoapFaultException(responseBody));
						return;
					}
					else
						throw new SoapFaultException(responseBody);
				}
			}
			if (showSoap)
				context.setXmlVar(resultVar, response);
			else
				context.setXmlVar(resultVar, responseBody);
			ok=true;
		}
		finally {
			if (! ok)
				Node.delete(response);
		}
	}
}
