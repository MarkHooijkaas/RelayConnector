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

import java.io.ByteArrayInputStream;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.util.NomNode;

import com.eibus.xml.nom.Node;

public class GroovyStep implements Step {
	private final static GroovyClassLoader loader = new GroovyClassLoader();
	private final Class scriptClass;
	
	
	public GroovyStep(CompilationContext compiler, final int node) {
		String scriptText = Node.getData(node);
		scriptClass = loader.parseClass(new ByteArrayInputStream(scriptText.toString().getBytes()), "dummySource");
	}

	public void executeStep(ExecutionContext context) {
		try {
			// Does each new invocation need a new instance?
			// probably, considering the bindings
			if (context.debugTraceEnabled())
				context.traceDebug("executing Groovy script class "+scriptClass.getName());
			Script script = (Script) scriptClass.newInstance();
			script.getBinding().setVariable("context", context);
			script.getBinding().setVariable("input",  new NomNode(context.getXmlVar("input")));
			script.getBinding().setVariable("output", new NomNode(context.getXmlVar("output")));
			script.run();
		} 
		catch (InstantiationException e) { throw new RuntimeException(e); }
		catch (IllegalAccessException e) { throw new RuntimeException(e); }
	}
}
