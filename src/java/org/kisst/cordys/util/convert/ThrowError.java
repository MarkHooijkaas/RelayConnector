package org.kisst.cordys.util.convert;

import com.eibus.xml.nom.Node;

public class ThrowError  implements Convertor {
	private final String ifEquals;
	private final String ifNotEquals;
	private final String errortext;
	
	public ThrowError(int node) {
		ifEquals= Node.getAttribute(node, "ifEquals");
		ifNotEquals = Node.getAttribute(node, "ifNotEquals");
		errortext = Node.getAttribute(node, "errortext");
	}
	
	public String convert(String origstr) {
		String errormsg=null;
		String str=origstr.trim();
		if (str.equals(ifEquals))
			errormsg ="AS/400 output is "+str;
		if (ifNotEquals!=null && str.equals(ifNotEquals))
			errormsg ="AS/400 output is not "+ifNotEquals;
		if (errormsg==null)
			return origstr;
		if (errortext!=null)
			errormsg=errortext;
		throw new RuntimeException (errormsg);
	}
}
