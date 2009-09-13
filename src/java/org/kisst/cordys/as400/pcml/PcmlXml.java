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
