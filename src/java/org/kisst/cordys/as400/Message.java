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

package org.kisst.cordys.as400;

import java.util.Locale;
import com.eibus.localization.ILocalizableString;

public class Message implements ILocalizableString {
	final private static Locale[] locales = new Locale[] { Locale.ENGLISH }; 
	final private String msg;
	public Message(String msg) { this.msg=msg; }
	public Locale[] getAvailableLocales() {	return locales;	}
	public String getMessage(Locale arg0) {	return msg;	}
}
