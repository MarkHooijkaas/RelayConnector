package org.kisst.cordys.as400.pcml;

import com.ibm.as400.access.AS400DataType;


public interface PcmlParameter {
	public String getName();
	public AS400DataType getDataType();

	public boolean isInput();
	public boolean isOutput();
	
	public Object parseInputNode(int inputNode);
	public boolean appendToOutputNode(int outputNode, Object obj);
	
	public boolean isOptional();
	public boolean hasDefaultValue();
	public Object getDefaultValue();
}
