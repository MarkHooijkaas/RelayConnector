package org.kisst.cordys.as400.pcml;

import org.kisst.cordys.script.CompilationContext;


public class PcmlFloat8 extends PcmlDataElement {
	public PcmlFloat8(CompilationContext context, PcmlStruct parent, int node) { 
		super(context, parent, node, new com.ibm.as400.access.AS400Float8()); 
	}

	@Override
	protected Object createObject(String value) {
		return Double.parseDouble(value);
	}
}
