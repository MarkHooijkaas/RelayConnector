package org.kisst.cordys.as400.pcml;

import org.kisst.cordys.script.CompilationContext;

public class PcmlInt2 extends PcmlDataElement {

	public PcmlInt2(CompilationContext context, PcmlStruct parent, int node) { 
		super(context, parent, node, new com.ibm.as400.access.AS400Bin2()); 
	}

	@Override
	protected Object createObject(String value) {
		return Short.parseShort(value);
	}
}
