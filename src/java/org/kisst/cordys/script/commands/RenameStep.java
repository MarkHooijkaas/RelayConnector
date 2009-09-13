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

import java.util.List;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.XmlExpression;
import org.kisst.cordys.util.NomNode;

import com.eibus.xml.nom.Node;

public class RenameStep implements Step {
	private final XmlExpression nodes;
	private final String name;
	private final String namespace;
	private final String prefix;
	
	public RenameStep(CompilationContext compiler, final int node) {
		nodes=new XmlExpression(compiler, Node.getAttribute(node, "nodes"));
		compiler.declareTextVar("it"); // TODO: remove after compilation
		name=Node.getAttribute(node, "name");
		prefix=Node.getAttribute(node, "prefix");
		namespace= compiler.getCallContext().resolvePrefix(prefix, Node.getAttribute(node, "namespace"));
	}

	public void executeStep(ExecutionContext context) {
		List<NomNode> nodeList=nodes.getNodeList(context);
		for (NomNode n: nodeList) {
			if (name!=null)
				n.rename(name);
			if (namespace!=null || prefix !=null)
				n.setNamespace(namespace, prefix);
		}
	}
}
