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

package org.kisst.cordys.as400;

import org.kisst.cordys.as400.conn.As400ConnectionPool;
import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.script.Step;
import org.kisst.cordys.script.expression.Expression;
import org.kisst.cordys.script.expression.ExpressionParser;
import org.kisst.cordys.util.NomUtil;

import com.eibus.xml.nom.Node;

public class StatusAs400PoolStep implements Step {

	private final Expression poolNameExpr;
	private final String varName;
	
	public StatusAs400PoolStep(CompilationContext compiler, final int node) {
		poolNameExpr=ExpressionParser.parse(compiler, Node.getAttribute(node, "pool"));
		varName=NomUtil.getStringAttribute(node, "var", "status");
		compiler.declareTextVar(varName);
	}

	public void executeStep(ExecutionContext context) {
		String poolName = poolNameExpr.getString(context);
		As400Module module = (As400Module) context.getBaseConnector().getModule(As400Module.class);
		As400ConnectionPool pool = module.getPool(poolName);
		context.setTextVar(varName, pool.status());
	}

}
