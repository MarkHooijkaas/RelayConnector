package org.kisst.cordys.script.commands;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.util.NomNode;
import org.kisst.cordys.util.ReflectionUtil;
import org.kisst.cordys.util.convert.Convertor;

import com.eibus.xml.nom.Node;

public class ConvertStep implements Step {
	private final XmlExpression expr;
	private final Convertor convertor;
	
	public ConvertStep(CompilationContext compiler, final int node) {
		String exprStr=Node.getAttribute(node,	"node");
		expr=new XmlExpression(compiler, exprStr);

		String classname= Node.getAttribute(node, "convertor");

		if (classname==null || classname.trim().length()==0)
			convertor=null;
		else {
			if (classname.indexOf('.')<0)
				classname="org.kisst.cordys.util.convert."+classname;
			try {
				Class cls = Class.forName(classname);
				Constructor cons = ReflectionUtil.getConstructor(cls, new Class[] {int.class});
				if (cons != null )
					convertor = (Convertor) cons.newInstance(new Object[] {node});
				else if (ReflectionUtil.getConstructor(cls, new Class[] {})!=null) // has default constructor
					convertor = (Convertor) cls.newInstance();
				else
					throw new RuntimeException("No default or (int) constructor for class "+classname);
			}
			catch (InstantiationException e) { throw new RuntimeException(e); }
			catch (IllegalAccessException e) { throw new RuntimeException(e); }
			catch (ClassNotFoundException e) { throw new RuntimeException(e); }
			catch (InvocationTargetException e) { throw new RuntimeException(e); }
		}
	}

	public void executeStep(final ExecutionContext context) {
		List<NomNode> nodeList=expr.getNodeList(context);
		for (NomNode n: nodeList)
			n.setText(convertor.convert(n.getText()));
	}
}
