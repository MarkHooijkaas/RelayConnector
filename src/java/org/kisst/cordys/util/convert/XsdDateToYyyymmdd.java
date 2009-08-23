package org.kisst.cordys.util.convert;

public class XsdDateToYyyymmdd  implements Convertor {
	public String convert(String str) {
		return str.substring(0,4)+str.substring(5,2)+str.substring(8,2);
	}
}
