package org.kisst.cordys.util;


public class DnUtil {
	
	public static String getFirstDnPart(String dn) {
		int pos=dn.indexOf('=');
		int pos2=dn.indexOf(',',pos);
		if (pos<0 ||pos2<0)
			return dn;
		else
			return dn.substring(pos+1, pos2);
	}
	
}

