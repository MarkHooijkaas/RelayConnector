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

import java.lang.reflect.Constructor;
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
			Class<?> cls = ReflectionUtil.findClass(classname);
			Constructor<?> cons = ReflectionUtil.getConstructor(cls, new Class[] {int.class});
			if (cons != null )
				convertor = (Convertor) ReflectionUtil.createObject(cons,new Object[] {node});
			else if (ReflectionUtil.getConstructor(cls, new Class[] {})!=null) // has default constructor
				convertor = (Convertor) ReflectionUtil.createObject(cls);
			else
				throw new RuntimeException("No default or (int) constructor for class "+classname);
		}
	}

	public void executeStep(final ExecutionContext context) {
		List<NomNode> nodeList=expr.getNodeList(context);
		for (NomNode n: nodeList) {
			if (n.containsOnlyText())
				n.setText(convertor.convert(n.getText()));
		}
	}
}