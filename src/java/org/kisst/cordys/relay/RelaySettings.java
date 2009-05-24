package org.kisst.cordys.relay;

import java.util.Properties;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cfg4j.Setting;
import org.kisst.cfg4j.StringSetting;

public class RelaySettings extends CompositeSetting {
	private Properties properties = new Properties();

	public final IntSetting timeout;
	public final BooleanSetting cacheScripts;
	public final StringSetting logSoapFaults;

	public RelaySettings(Setting parent, String name, Properties props) { 
		super(parent, name); 
		this.properties=props;
		timeout=new IntSetting(this, "timeout",props, 20000);
		cacheScripts=new BooleanSetting(this, "cacheScripts",props,  false);
		logSoapFaults=new StringSetting(this, "logSoapFaults", props, null);
	}
	public String get(String key) { return properties.getProperty(key); }
	public String get(String key, String defaultValue) { return properties.getProperty(key,defaultValue); }
}
