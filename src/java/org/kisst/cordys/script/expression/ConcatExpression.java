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

import java.util.regex.Pattern;

import org.kisst.cordys.script.CompilationContext;
import org.kisst.cordys.script.ExecutionContext;

public class ConcatExpression implements Expression {
	private static Pattern splitter = Pattern.compile("[+]");

	private final Expression[] expressions;
	
	public ConcatExpression(CompilationContext compiler, final String str) {
		String[] parts=splitter.split(str);
		expressions = new Expression[parts.length];
		for(int i=0; i<parts.length; i++) {
			expressions[i]=ExpressionParser.parse(compiler,parts[i]);
		}
	}

	public String getString(ExecutionContext context) {
		String result = expressions[0].getString(context);
		for (int i=1; i<expressions.length; i++)
			result += expressions[i].getString(context);
		return result;
	}

}
