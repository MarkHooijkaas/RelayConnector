package org.kisst.cordys.script.commands;

import java.util.Date;

import org.kisst.cordys.relay.MethodCache;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.RelayTrace;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.script.xml.ElementAppender;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.connector.nom.Connector;
import com.eibus.connector.nom.SOAPMessageListener;
import com.eibus.directory.soap.DirectoryException;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class MethodCall {
	//private static final CordysLogger logger=CordysLogger.getCordysLogger(MethodCall.class);
	
	private final String namespace;
	private final Expression namespaceExpression;
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

		expr=Node.getAttribute(node, "namespaceExpression");
		if (expr!=null)
			namespaceExpression=ExpressionParser.parse(compiler,expr);
		else
			namespaceExpression=null;
		if (namespaceExpression==null)
			namespace=compiler.getSmartAttribute(node, "namespace", null);
		else
			namespace=Node.getAttribute(node, "namespace");
		if (namespace==null && namespaceExpression==null)
			throw new RuntimeException("attribute namespace or namespaceExpression should be set or a default should be defined");
		if (namespace!=null && namespaceExpression!=null)
			throw new RuntimeException("attribute namespace and namespaceExpression should not be set both");


		async=compiler.getSmartBooleanAttribute(node, "async", false);
		showSoap=compiler.getSmartBooleanAttribute(node, "showSoap", false);
		ignoreSoapFault=compiler.getSmartBooleanAttribute(node, "ignoreSoapFault", false);
		logSoapFault=RelayTrace.parseSeverity(compiler.getSmartAttribute(node, "logSoapFault", compiler.getSettings().logSoapFaults.get()));
		appender=new ElementAppender(compiler, node);
		String appendMessagesToString = compiler.getSmartAttribute(node, "appendMessagesTo", null);
		if (appendMessagesToString==null)
			appendMessagesTo=null;
		else
			appendMessagesTo= new XmlExpression(compiler, appendMessagesToString);
		
		String tmpName=Node.getAttribute(node, "resultVar");
		
		resultVar = tmpName==null? defaultResultVar : tmpName;
		if (resultVar==null)
			throw new RuntimeException("resultVar should be defined when using methodExpression");
		compiler.declareXmlVar(resultVar);
		String timeoutString=Node.getAttribute(node, "timeout");
		if (timeoutString==null)
			timeout=compiler.getSettings().timeout.get();
		else
			timeout=Long.parseLong(timeoutString);
	}
	
	protected int createMethod(final ExecutionContext context) {
		String effectiveNamespace = getNamespace(context);
		if (effectiveNamespace == null )
			effectiveNamespace = context.getTextVar("namespace");
		String dnUser=context.getOrganizationalUser();
		String dnOrganization=context.getOrganization();
		Connector connector = context.getRelayConnector().getConnector();
		int method;
		String m=getMethodName(context);
		try {
			if (m==null)
				m=methodExpression.getString(context);
			method = connector.createSOAPMethod(dnUser, dnOrganization, effectiveNamespace, m);
		} 
		catch (DirectoryException e) {  throw new RuntimeException("Error when handling method "+m,e); }
		appender.append(context, method);
		return method;

	}

	protected String getNamespace(final ExecutionContext context)  {
		if (namespace!=null)
			return namespace;
		else 
			return namespaceExpression.getString(context);
	}
	protected String getMethodName(final ExecutionContext context) { 
		if (methodName!=null)
			return methodName;
		else 
			return methodExpression.getString(context);
	}

	// TODO: fix possible memory leak if async calls never return a answer
	// Such call will register a SOAPListener which will never be removed.
	// In long term this could lead to a OutOfMemory error.
	// Possible fix would be to not use sendAndCallback, but just send, and use a 
	// default SOAPListener. This in considered not very urgent yet.
	
	@SuppressWarnings("deprecation")
	protected void callMethod(final ExecutionContext context, int method) {
		if (appendMessagesTo!=null) {
			int logNode=appendMessagesTo.getNode(context);
			Node.duplicateAndAppendToChildren(method, method, logNode);
		}
		if (context.infoTraceEnabled())
			context.traceInfo("sending request\n"+Node.writeToString(method, true));
		MethodCache caller = context.getRelayConnector().responseCache;
		if (async) {
			context.createXmlSlot(resultVar, getMethodName(context), new Date().getTime()+timeout);
			caller.sendAndCallback(Node.getParent(method),new SOAPMessageListener() {
				public boolean onReceive(int message)
				{
					handleResponse(context, message);
					return false; // Node should not yet be destroyed by Callback caller!!
				}
			});
		}
		else {
			handleResponse(context, caller.sendAndWait(method,timeout));
		}
	}

	private void handleResponse(ExecutionContext context, int response) {
		boolean ok=false;
		try {
			if (context.infoTraceEnabled())
				context.traceInfo("received response\n"+Node.writeToString(response, true));
			if (appendMessagesTo!=null) {
				int logNode=appendMessagesTo.getNode(context);
				Node.duplicateAndAppendToChildren(response, response, logNode);
			}
			int responseBody=SoapUtil.getContent(response);
			if (SoapUtil.isSoapFault(responseBody)) {
				if (logSoapFault!=null)
					context.trace(logSoapFault, "Calling method "+getMethodName(context)+" returned Fault: "+Node.writeToString(responseBody, true));
				if (! ignoreSoapFault) {
					if (async) {
						context.setAsynchronousError(new RelaySoapFaultException(responseBody));
						return;
					}
					else
						throw new RelaySoapFaultException(responseBody);
				}
			}
			if (showSoap)
				context.setXmlVar(resultVar, response);
			else
				context.setXmlVar(resultVar, responseBody, response);
			ok=true;
		}
		finally {
			if (! ok)
				Node.delete(response);
		}
	}
}