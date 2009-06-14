package org.kisst.cordys.util;

import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.Setting;

import com.eibus.util.logger.Severity;

public class SeveritySetting extends Setting {
	private final Severity defaultValue;

	public SeveritySetting(CompositeSetting parent, String name, Severity defaultValue) {
		super(parent, name);
		this.defaultValue=defaultValue;
	}
	public Severity get(Props props) {	
		String str=props.getString(fullName, null);
		if (str==null)
			return defaultValue;
		else
			return parseSeverity(str);
	}
	
	private static Severity parseSeverity(String sev) {
		if (sev==null)           return null;
		if (sev.equals("NONE"))  return null;
		if (sev.equals("DEBUG")) return Severity.DEBUG;
		if (sev.equals("INFO"))  return Severity.INFO;
		if (sev.equals("WARN"))  return Severity.WARN;
		if (sev.equals("ERROR")) return Severity.ERROR;
		if (sev.equals("FATAL")) return Severity.FATAL;
		throw new RuntimeException("unknown Severity ["+sev+"] should be NONE, DEBUG, INFO, WARN, ERROR or FATAL");
	}
}
