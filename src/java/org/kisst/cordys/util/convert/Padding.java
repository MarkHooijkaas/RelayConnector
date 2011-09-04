package org.kisst.cordys.util.convert;

import com.eibus.xml.nom.Node;

public class Padding implements Convertor{
	private final char with;
	private String padTo;
	private final int padLength;
	
	public Padding (int node){
		with= Node.getAttribute(node, "with").charAt(0);
		padTo= Node.getAttribute(node, "padTo");
		if (padTo==null)
			padTo="left";
		
		String padL=Node.getAttribute(node, "padLength");
		if (padL==null)
			padLength=Integer.parseInt( Node.getAttribute(node, "length"));
		else	
			padLength=Integer.parseInt( Node.getAttribute(node, "padLength"));
		
	}
	
	public String convert(String str) {

		if (padTo.equals("right"))
			return String.format("%-" + padLength + "s", str).replace(' ', with);	
		if (padTo.equals("left"))
			return String.format("%" + padLength + "s", str).replace(' ', with);
		throw new RuntimeException ("Value '"+ padTo + "' is not valid value for padTo. Use 'left' or 'rigth'" );		
		
	}
}

