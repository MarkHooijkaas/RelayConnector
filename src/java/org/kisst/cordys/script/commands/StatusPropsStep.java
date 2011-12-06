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
import org.kisst.cordys.util.NomUtil;
import org.kisst.props4j.SimpleProps;

public class StatusPropsStep implements Step {

	private final String varName;
	
	public StatusPropsStep(CompilationContext compiler, final int node) {
		varName=NomUtil.getStringAttribute(node, "var", "statusProps");
		compiler.declareTextVar(varName);
	}

	public void executeStep(ExecutionContext context) {
		SimpleProps props = (SimpleProps) context.getBaseConnector().getProps();
		context.setTextVar(varName, "#\n"+props.toPropertiesString()+"\n#");
	}

}
