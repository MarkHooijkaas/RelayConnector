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
import org.kisst.cordys.util.NomPath;
import org.kisst.cordys.util.NomUtil;

import com.eibus.xml.nom.Node;
import com.ibm.as400.access.AS400Array;
import com.ibm.as400.access.AS400DataType;

public abstract class PcmlElement implements PcmlParameter {

	private final PcmlElement parent;
	private final String name;
	private final String prefix;
	private final String namespace;
	private final String usage;
	private final boolean useForInput;
	private final boolean useForOutput;
	private final NomPath xmlInput;
	private final NomPath xmlOutput;
	private final boolean optional;
	private final int count;
	protected final AS400DataType elementType;
	protected final AS400Array arrayType;

	public boolean isInput() { return useForInput; }
	public boolean isOutput() { return useForOutput; }

	public PcmlElement(CompilationContext context, PcmlElement parent, int node, AS400DataType type)
    {
		this.parent=parent;
		this.elementType=type;

		name = Node.getAttribute(node, "name");
		prefix = Node.getAttribute(node, "prefix");
		namespace = Node.getAttribute(node, "namespace");
		optional = NomUtil.getBooleanAttribute(node, "optional", false);
		
		String path=NomUtil.getStringAttribute(node, "xmlInput", name);
		if (path==null || path.trim().length()==0)
			xmlInput=null;
		else
			xmlInput  = new NomPath(context, path, optional);
		
		path=NomUtil.getStringAttribute(node, "xmlOutput", name);
		if (path==null || path.trim().length()==0)
			xmlOutput=null;
		else
			xmlOutput = new NomPath(context, path, optional);
		
		usage = Node.getAttribute(node, "usage");

		count = NomUtil.getIntAttribute(node, "count", 1); // TODO: varaiable count field not supported yet
		if (count<1)
			throw new RuntimeException("attribute count should be >=1 (varaiable count is not yet supported)");			
		if (count>1) {
			this.arrayType=new AS400Array();
			this.arrayType.setType(type);
			this.arrayType.setNumberOfElements(count);
		}
		else
			this.arrayType=null;
		
		String actualUsage=getActualUsage();
		if ("input".equals(actualUsage)) {
			useForInput=true;
			useForOutput=false;
		}
		else if ("output".equals(actualUsage)) {
			useForInput=false;
			useForOutput=true;
		}
		else if ("inputoutput".equals(actualUsage)) {
			useForInput=true;
			useForOutput=true;
		}
		else 
			throw new RuntimeException("usage attribute (after inherit resolution) should be input, output or inputoutput");
    }

	public AS400DataType getDataType() {
		if (arrayType==null)
			return elementType;
		else
			return arrayType;
	}
	

	public String getName() { return name; }
	public String getUsage() { return usage; }
	public String getActualUsage() { 
		if (parent==null && usage==null)
			return "inputoutput";
		else if ("inherit".equals(usage) || usage==null) // TODO: is this correct interpretation
			return parent.getActualUsage();
		else
			return usage;
	}

	public boolean isOptional() { return optional; }
	
	abstract protected Object parseSingleNode(int inputNode);
	public final Object parseInputNode(int inputNode) {
		if (! useForInput) // TODO: is this check necessary?
			return 0;
		if (count==1) {
			int subNode;
			if (xmlInput==null)
				subNode=0;
			else
				subNode = xmlInput.findNode(inputNode);
			if (subNode==0) {
				if (isOptional())
					return getDefaultValue();
				else
					throw new RuntimeException("Could not find field "+xmlInput+ " and not optional");
			}
			
			return parseSingleNode(subNode);

		}
		Object[] objs=new Object[count];
		int i=0;
		int node=Node.getFirstElement(inputNode);
		while (node!=0 && i<count) {
			// ugly, test each sibling for the correct name
			// no other way to get all children with a specific name
			if (name.equals(Node.getLocalName(node))) { // TODO: use xmlInput instead of name
	    		try {
	    			objs[i]=parseSingleNode(node);
	    		}
	    		catch (Exception e) {
	    			throw new RuntimeException("Error when preparing input of occurrence "+i+" for parameter "+xmlInput+": "+e.toString(),e);
	    		}
				i++;
			}
			node = Node.getNextSibling(node);
			// TODO: warn if i>=count, because extra elements will be ignored
		}
		// TODO: test if it is also allowed to use a smaller array than in the count PCML field, so we dont need to add all
		// the empty elements
		while (i<count) {
			objs[i]=getDefaultValue();
			i++;
		}
		return objs;
	}
	
	public Object getDefaultValue() {
		return elementType.getDefaultValue();
	}
	
	protected int createOutputSubNode(int outputNode, String text) {
		int node;
		if (xmlOutput==null) // do not create a new node
			return 0; 
		else if (text==null || text.length()==0)
			node = xmlOutput.findNodeWithCreate(outputNode);
		else
			node=xmlOutput.setText(outputNode, text);
		if (namespace!=null) {
			if (prefix==null)
				Node.setAttribute(node, "xmlns", namespace);
			else
				Node.setAttribute(node, "xmlns:"+prefix, namespace);
		}
		if (prefix!=null)
			Node.setName(node, prefix+":"+name);
		return node;
	}

	public abstract boolean appendSingleObjectToOutputNode(int outputNode, Object obj);
	public boolean appendToOutputNode(int outputNode, Object obj) {
		if (count==1)
			return appendSingleObjectToOutputNode(outputNode, obj);
		else {
			boolean empty=true;
			Object[] arr= (Object[]) obj;
			if (arr.length !=count)
				throw new RuntimeException("Expected "+count+" output elements for parameter "+getName()+", but got "+arr.length );
			for (int i=0; i<count; i++)
				// TODO: don't add empty elements (but when is it empty?)
				empty = appendSingleObjectToOutputNode(outputNode, arr[i]) && empty;
			return empty;
		}
	}

}