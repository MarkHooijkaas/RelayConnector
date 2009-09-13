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

import org.kisst.cordys.script.CompilationContext;


public class ExpressionParser {
	
	public static Expression parse(CompilationContext compiler, String str) {
		if (str==null)
			return null;
		str=str.trim();
		// TODO: much better parser: This will break in many situations
		// e.g. + embedded in strings, and many other nested scenarios
		if (str.indexOf('+')>=0)
			return new ConcatExpression(compiler, str);
		if (str.startsWith("/"))
			return new XmlExpression(compiler, str);
		if (str.startsWith("${"))
			return new VarExpression(compiler, str);
		if (str.startsWith("["))
			return new ConstantExpression(str.substring(str.indexOf('[')+1, str.lastIndexOf(']')));
		if (str.indexOf("::")>0)
			return new JavaExpression(compiler,str);
		return new ConstantExpression(str);
	}
	
}
