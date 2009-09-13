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
import org.kisst.cordys.script.Step;

import com.eibus.xml.nom.Node;

public class XmlnsStep implements Step {
	
	public XmlnsStep(CompilationContext compiler, final int node) {
		String prefix=Node.getAttribute(node, "prefix");
		String namespace=Node.getAttribute(node, "namespace");
		if (prefix==null)
			throw new RuntimeException("in command <xmlns...> there should be an attribute with the name \"prefix\"");
		if (namespace==null)
			throw new RuntimeException("in command <xmlns prefix=\""+prefix+"\" ...> there should be an attribute with the name \"namespace\"");
		compiler.addPrefix(prefix, namespace);
	}

	public void executeStep(ExecutionContext context) {
		// Do nothing, this is a compile time step
	}
}
