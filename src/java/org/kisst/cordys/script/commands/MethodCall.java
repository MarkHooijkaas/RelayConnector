package org.kisst.cordys.script.commands;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;
import org.kisst.cordys.script.xml.ElementAppender;

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
		appender=new ElementAppender(compiler, node);
		
		String tmpName=Node.getAttribute(node, "resultVar");
		
		resultVar = tmpName==null? defaultResultVar : tmpName;
		if (resultVar==null)
			throw new RuntimeException("resultVar should be defined when using methodExpression");
		compiler.declareXmlVar(resultVar);
	}
	
	protected int createMethod(final ExecutionContext context) {
		String ns = getNamespace(context);
		String mname=getMethodName(context);
		int method = context.createMethod(ns, mname).node;
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

	protected void callMethod(final ExecutionContext context, int method) {
		if (async)
			context.callMethodAsync(method, resultVar);
		else
			context.callMethod(method, resultVar);
	}
}