package org.kisst.cordys.util.convert;

public class YyyymmddToXsdDate  implements Convertor {
	public String convert(String str) {
		return str.substring(0,4)+"-"+str.substring(4,2)+"-"+str.substring(6,2);
	}
}
