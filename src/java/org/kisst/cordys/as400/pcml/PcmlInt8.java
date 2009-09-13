package org.kisst.cordys.as400.pcml;

import org.kisst.cordys.script.CompilationContext;


public class PcmlInt8 extends PcmlDataElement {

	public PcmlInt8(CompilationContext context, PcmlStruct parent, int node) { 
		super(context, parent, node, new com.ibm.as400.access.AS400Bin8()); 
	}

	@Override
	protected Object createObject(String value) {
		return Long.parseLong(value);
	}
}
