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

package org.kisst.cordys.script.expression;

import java.util.ArrayList;
import java.util.List;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.util.NomNode;
import org.kisst.cordys.util.NomPath;

import com.eibus.xml.nom.Node;

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
			return Node.getData(node);
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

	public List<NomNode> getNodeList(ExecutionContext context) {
		int node=context.getXmlVar(name);
		if (path==null) {
			ArrayList<NomNode> result = new ArrayList<NomNode>();
			result.add(new NomNode(node));
			return result;
		}
		else
			return path.getNodeList(node);
	}

}
