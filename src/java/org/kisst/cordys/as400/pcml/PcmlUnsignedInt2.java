package org.kisst.cordys.as400.pcml;

import org.kisst.cordys.script.CompilationContext;

public class PcmlUnsignedInt2 extends PcmlDataElement {

	public PcmlUnsignedInt2(CompilationContext context, PcmlStruct parent, int node) { 
		super(context, parent, node, new com.ibm.as400.access.AS400UnsignedBin2()); 
	}

	@Override
	protected Object createObject(String value) {
		return Integer.parseInt(value);
	}
}
