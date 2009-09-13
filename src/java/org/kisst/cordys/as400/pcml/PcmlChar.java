package org.kisst.cordys.as400.pcml;

import org.kisst.cordys.as400.As400Module;
import org.kisst.cordys.script.CompilationContext;

import com.ibm.as400.access.AS400Text;


public class PcmlChar extends PcmlDataElement {

	public PcmlChar(CompilationContext context, PcmlStruct parent, int node) {
		super(context, parent, node, new AS400Text(TextUtil.parseLengthAttribute(node), As400Module.getCcsid()));
	}

	@Override
	protected Object createObject(String value) {
		int maxSize=elementType.getByteLength();
		if (value.length()>maxSize)
			value=value.substring(maxSize);
		return value;
	}
}
