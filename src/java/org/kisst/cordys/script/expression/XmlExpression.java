package org.kisst.cordys.script.expression;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.util.NomPath;

public class XmlExpression  implements Expression {
	private final String name;
	private final NomPath path;
	
	public XmlExpression(CompilationContext compiler, String str) {
		str=str.trim();
		if (! str.startsWith("/"))
			throw new IllegalArgumentException("XML expression should start with a /");
		while (str.endsWith("/"))
			str=str.substring(0, str.length()-1);
		int pos=str.indexOf('/',1);
		if (pos<0) {
			name=str.substring(1);
			path=null;
		}
		else {
			name=str.substring(1,pos);
			path=new NomPath(compiler, str.substring(pos+1));
		}
		if (! compiler.xmlVarExists(name))
			throw new RuntimeException("xml expression ["+str+"] refers to non declared xmlvar "+name);
		
	}
	
	public String getString(ExecutionContext context) {
		int node=context.getXmlVar(name);
		if (path==null)
			return null; // TODO: can this happen?
		else
			return path.getText(node);
	}

	public int getNode(ExecutionContext context) {
		int node=context.getXmlVar(name);
		if (path==null)
			return node;
		else
			return path.findNode(node);
	}

}

