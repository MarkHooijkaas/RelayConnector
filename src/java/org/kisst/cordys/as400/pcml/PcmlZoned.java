package org.kisst.cordys.as400.pcml;

import java.math.BigDecimal;

import org.kisst.cordys.script.CompilationContext;

public class PcmlZoned extends PcmlDataElement {

	public PcmlZoned(CompilationContext context, PcmlStruct parent, int node) { 
		super(context, parent, node, new com.ibm.as400.access.AS400ZonedDecimal(
				TextUtil.parseLengthAttribute(node),TextUtil.parsePrecisionAttribute(node))); 
	}

	@Override
	protected Object createObject(String value) {
		return new BigDecimal(value);
	}
}
