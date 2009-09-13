package org.kisst.cordys.as400.pcml;

import org.kisst.cordys.script.CompilationContext;

public class PcmlInt4 extends PcmlDataElement {

	public PcmlInt4(CompilationContext context, PcmlStruct parent, int node) { 
		super(context, parent, node, new com.ibm.as400.access.AS400Bin4()); 
	}

	@Override
	protected Object createObject(String value) {
		return Integer.parseInt(value);
	}
}
