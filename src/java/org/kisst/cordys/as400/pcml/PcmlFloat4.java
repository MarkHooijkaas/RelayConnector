package org.kisst.cordys.as400.pcml;

import org.kisst.cordys.script.CompilationContext;


public class PcmlFloat4 extends PcmlDataElement {
	public PcmlFloat4(CompilationContext context, PcmlStruct parent, int node) { 
		super(context, parent, node, new com.ibm.as400.access.AS400Float4()); 
	}

	@Override
	protected Object createObject(String value) {
		return Float.parseFloat(value);
	}
}
