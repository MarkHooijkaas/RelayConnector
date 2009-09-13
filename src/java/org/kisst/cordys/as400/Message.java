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
