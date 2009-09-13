package org.kisst.cordys.as400.pcml;

import org.kisst.cordys.script.CompilationContext;

public class PcmlUnsignedInt4 extends PcmlDataElement {

	public PcmlUnsignedInt4(CompilationContext context, PcmlStruct parent, int node) { 
		super(context, parent, node, new com.ibm.as400.access.AS400UnsignedBin4()); 
	}

	@Override
	protected Object createObject(String value) {
		return Long.parseLong(value);
	}
}
