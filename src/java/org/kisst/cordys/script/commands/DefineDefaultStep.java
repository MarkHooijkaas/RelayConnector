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

public class DefineDefaultStep implements Step {
	
	public DefineDefaultStep(CompilationContext compiler, final int node) {
		String attr=Node.getAttribute(node, "attribute");
		String value=Node.getAttribute(node, "value");
		if (attr==null)
			throw new RuntimeException("in command <default ...> there should be an attribute with the name \"attribute\"");
		if (value==null)
			throw new RuntimeException("in command <default attribute=\""+attr+"\" ...> there should be an attribute with the name \"value\"");
		compiler.setDefaultAttribute(attr, value);
	}

	public void executeStep(ExecutionContext context) {
		// Do nothing, this is a compile time step
	}
}
