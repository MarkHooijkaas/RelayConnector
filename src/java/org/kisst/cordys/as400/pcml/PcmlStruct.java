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

import org.kisst.cordys.script.CompilationContext;

import com.eibus.xml.nom.Node;
import com.ibm.as400.access.AS400DataType;
import com.ibm.as400.access.AS400Structure;

public class PcmlStruct extends PcmlElement {

	protected final PcmlParameter[] params;

	public PcmlStruct(CompilationContext context, PcmlStruct parent, int structNode)
    {
		super(context, parent, structNode, new AS400Structure());
		int paramCount=Node.getNumChildren(structNode);
    	params = new PcmlParameter[paramCount];
    	AS400DataType[] types = new AS400DataType[paramCount];
    	
    	int i=0;
    	int node = Node.getFirstChild(structNode);
    	while (node != 0) {
    		String maintype=Node.getLocalName(node);
    		try {
    			if ("data".equals(maintype))
    				params[i] = createDataParameter(context, node);
    			else if ("struct".equals(maintype))
    				params[i] = new PcmlStruct(context, this, node);
    			else {
    				throw new RuntimeException("Unknown element <"+maintype+">");
    			}
    			
    		}
    		catch (Exception e) {
    			// This Exception is wrapped with some extra information about what is being parsed
    			throw new RuntimeException("Error while parsing "+
    					Node.writeToString(node, false)
    					+" of struct "+getName()+": "+e.toString(),e);
    		}
    		node = Node.getNextSibling(node);
    		types[i]=params[i].getDataType();
    		i++;
    	}
    	
		if (isInput() && isOptional() && ! hasDefaultValue()) {
			throw new RuntimeException("Input struct "+getName()+" is optional but at least one of it's (indirect) children does not have any attribute init or emptyValue");
		}

		((AS400Structure)elementType).setMembers(types);
		// It is necessary to again set the DataType of an array, because the first
		// time this was done in the conatructor, the members were not yet known. 
		if (arrayType!=null)
			arrayType.setType(elementType);
    }

	
	private PcmlParameter createDataParameter(CompilationContext context, int node) {
		String datatype = Node.getAttribute(node, "type");
		String length= Node.getAttribute(node, "length");
		String precision= Node.getAttribute(node, "precision");
		if (datatype==null)
			throw new RuntimeException("attribute type should be specified in PCML");
		if ("char".equals(datatype)) {
			return new PcmlChar(context, this, node);
		}
		if ("xml".equals(datatype)) {
			return new PcmlXml(context, this, node);
		}
		if ("int".equals(datatype)) {
			if ("2".equals(length)) {
				if (precision==null || "15".equals(precision))
					return new PcmlInt2(context, this, node);
				if ("16".equals(precision))
					return new PcmlUnsignedInt2(context, this, node);
				throw new RuntimeException("precision should be 15 or 16 for type=\"int\", length=\"2\"  in PCML");
			}
			if ("4".equals(length)) {
				if (precision==null || "31".equals(precision))
					return new PcmlInt4(context, this, node);
				if ("32".equals(precision))
					return new PcmlUnsignedInt4(context, this, node);
				throw new RuntimeException("precision should be 31 or 32 for type=\"int\", length=\"4\"  in PCML");
			}
			if ("8".equals(length)) {
				if (precision==null || "63".equals(precision))
					return new PcmlInt8(context, this, node);
				throw new RuntimeException("precision should be 63 for type=\"int\", length=\"8\"  in PCML");
			}
			throw new RuntimeException("length should be 2, 4 or 8 for type=\"int\" in PCML");
		}
		if ("float".equals(datatype)) {
			if ("4".equals(length)) 
				return new PcmlFloat4(context, this, node);
			if ("8".equals(length)) 
				return new PcmlFloat8(context, this, node);
			throw new RuntimeException("length should be 4 or 8 for type=\"float\" in PCML");
		}
		if ("packed".equals(datatype))
			return new PcmlPacked(context, this, node);
		if ("zoned".equals(datatype))
			return new PcmlZoned(context, this, node);
		if ("byte".equals(datatype))
			throw new RuntimeException("type=\"byte\" not yet supported in PCML");
		if ("struct".equals(datatype))
			return new PcmlStruct(context, this,node);
		throw new RuntimeException("Unknown PCML type \""+datatype+"\"");
	}
	
	protected Object parseSingleNode(int structNode) {
		Object[] objs=new Object[params.length];
    	for(int i=0; i<params.length; i++) {
    		try {
    			objs[i]=params[i].parseInputNode(structNode);
    		}
    		catch (Exception e) {
    			throw new RuntimeException("Error when preparing input for parameter "+params[i].getName()+": "+e.toString(),e);
    		}
    		if (objs[i]==null)
    			throw new RuntimeException("Optional parameters not allowed within a struct when preparing input for parameter "+params[i].getName());
    	}
		return objs;
	}

	public boolean appendSingleObjectToOutputNode(int outputNode, Object obj) {
		boolean empty=true;
		Object[] objs=(Object[]) obj;
		int subNode = createOutputSubNode(outputNode, null);
		if (subNode==0)
			return true; // no node created, so it is empty
    	for(int i=0; i<params.length; i++) {
    		try {
    			empty = params[i].appendToOutputNode(subNode, objs[i]) && empty;
    		}
    		catch (Exception e) {
    			throw new RuntimeException("Error when parsing output for parameter "+params[i].getName()+": "+e.toString(),e);
    		}
    	}
    	if (empty && isOptional())
    		Node.delete(subNode);
    	return empty;
	}
	
	public boolean hasDefaultValue() {
    	for(int i=0; i<params.length; i++)
   			if (params[i].hasDefaultValue()==false)
   				return false;
    	return true;
	}
	
	public Object getDefaultValue() {
		Object[] objs=new Object[params.length];
    	for(int i=0; i<params.length; i++)
   			objs[i]=params[i].getDefaultValue();
		return objs;
	}
}