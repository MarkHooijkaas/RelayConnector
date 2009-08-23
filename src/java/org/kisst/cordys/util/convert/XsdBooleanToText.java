package org.kisst.cordys.util.convert;

import com.eibus.xml.nom.Node;

public class XsdBooleanToText  implements Convertor {
	private final String trueText;
	private final String falseText;
	
	public XsdBooleanToText(int node) {
		trueText= Node.getAttribute(node, "trueText");
		falseText= Node.getAttribute(node, "falseText");
	}
	
	public String convert(String str) {
		if (str.equals("1") || str.equals("true"))
			return trueText;
		if (str.equals("0") || str.equals("false"))
			return falseText;
		throw new RuntimeException ("Value '"+str+"' is not an xsd:date");
	}
}
