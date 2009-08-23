package org.kisst.cordys.util.convert;

import com.eibus.xml.nom.Node;

public class TextToXsdBoolean  implements Convertor {
	private final String trueText;
	private final String falseText;
	
	public TextToXsdBoolean(int node) {
		trueText= Node.getAttribute(node, "trueText");
		falseText= Node.getAttribute(node, "falseText");
	}
	
	public String convert(String str) {
		str=str.toLowerCase().trim();
		if (str.equals(trueText))
			return "true";
		if (str.equals(falseText))
			return "false";
		throw new RuntimeException ("Could not convert value '"+str+"' to a boolean value, expected "+trueText+"or "+falseText);
	}
}
