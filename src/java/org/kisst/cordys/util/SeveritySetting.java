/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
*/

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
