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

package org.kisst.cordys.util.convert;

import com.eibus.xml.nom.Node;

public class XsdBooleanToText  implements Convertor {
	private final String trueText;
	private final String falseText;
	
	public XsdBooleanToText(int node) {
		trueText= Node.getAttribute(node, "trueText");
		falseText= Node.getAttribute(node, "falseText");
	}
	
	public String convert(String str) {
		if (str.equals("1") || str.equals("true"))
			return trueText;
		if (str.equals("0") || str.equals("false"))
			return falseText;
		throw new RuntimeException ("Value '"+str+"' is not an xsd:date");
	}
}
