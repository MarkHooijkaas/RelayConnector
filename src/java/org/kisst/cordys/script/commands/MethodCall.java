/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.script.xml.ElementAppender;
import org.kisst.cordys.util.NomUtil;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.xml.nom.Node;

public class MethodCall {
	//private static final CordysLogger logger=CordysLogger.getCordysLogger(MethodCall.class);
	
	private final String namespace;
	private final Expression namespaceExpression;
	private final String methodName;
	private final Expression methodExpression;
	private final boolean async;
	private final ElementAppender appender;
	private final String resultVar;
	private final XmlExpression headerExpression;
	private final boolean ignoreSoapFault;

	
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

		ignoreSoapFault=NomUtil.getBooleanAttribute(node, "ignoreSoapFault", false);
		async=compiler.getSmartBooleanAttribute(node, "async", false);
		appender=new ElementAppender(compiler, node);
		
		String tmpName=Node.getAttribute(node, "resultVar");
		
		resultVar = tmpName==null? defaultResultVar : tmpName;
		if (resultVar==null)
			throw new RuntimeException("resultVar should be defined when using methodExpression");
		compiler.declareXmlVar(resultVar);
		
		String tmp=Node.getAttribute(node, "headersFrom");
		if (tmp==null)
			headerExpression=null;
		else
			headerExpression=new XmlExpression(compiler, tmp);
	}
	
	protected int createMethod(final ExecutionContext context) {
		String ns = getNamespace(context);
		String mname=getMethodName(context);
		int method = context.createMethod(ns, mname).node;
		appender.append(context, method);
		if (headerExpression!=null) {
			int headerSrc=headerExpression.getNode(context);
			SoapUtil.copyHeaders(headerSrc, method);
		}
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

	protected void callMethod(final ExecutionContext context, int method) {
		if (async)
			context.callMethodAsync(method, resultVar, ignoreSoapFault);
		else {
			int response = context.callMethod(method, resultVar, ignoreSoapFault);
			context.setXmlVar(resultVar, SoapUtil.getContent(response));
		}
	}
}