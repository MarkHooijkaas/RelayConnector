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

package org.kisst.cordys.as400.pcml;

import java.io.UnsupportedEncodingException;

import org.kisst.cordys.as400.As400Module;
import org.kisst.cordys.script.CompilationContext;


import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;
import com.ibm.as400.access.AS400Text;


public class PcmlXml extends PcmlElement {

	private final String defaultValue;

	public PcmlXml(CompilationContext context, PcmlStruct parent, int dataNode) {
		super(context, parent, dataNode, new AS400Text(TextUtil.parseLengthAttribute(dataNode), As400Module.getCcsid()));
		int defaultValueNode=Node.getElement(dataNode, getName());
		if (defaultValueNode==0)
			defaultValue=null;
		else
			defaultValue = Node.writeToString(defaultValueNode, false);
		
		// default behaviour is to trim strings, because leading/trailing spaces and XML
		// do not mix that well
		// TODO: maybe default should be false
		if (isOptional() && ! hasDefaultValue()) {
			throw new RuntimeException("data parameter "+getName()+" is optional but does not have any attribute init or emptyValue");
		}
	}

	protected Object parseSingleNode(int dataNode) {
		String fieldValue = Node.writeToString(dataNode,false);
		return fieldValue;
	}

	public boolean appendSingleObjectToOutputNode(int outputNode, Object obj) {
		String str=obj.toString();
		boolean empty = str.equals(defaultValue);
		if (empty && isOptional())
			return true;
		int xml=0;
		try {
			int node=createOutputSubNode(outputNode, null);
			if (node==0)
				return true; // no output node created, so an empty element

			xml=Node.getDocument(node).parseString(str);
			Node.duplicateAndAppendToChildren(xml, xml, node);
		} 
		catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
		catch (XMLException e) { throw new RuntimeException(e); }
		finally {
			if (xml!=0)
				Node.delete(xml);
		}
		return empty;
	}


	public boolean hasDefaultValue() {
		return defaultValue!=null;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}
}
