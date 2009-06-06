package org.kisst.cordys.relay;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.StringSetting;

public class RelaySettings extends CompositeSetting {

	public final IntSetting timeout=new IntSetting(this, "timeout",20000);
	public final BooleanSetting cacheScripts=new BooleanSetting(this, "cacheScripts", false);
	public final StringSetting logSoapFaults=new StringSetting(this, "logSoapFaults", null);
	public final BooleanSetting showStacktrace=new BooleanSetting(this, "showStacktrace",false);

	public RelaySettings(Props props, String name) { 
		super(props, name); 
	}
	public String get(String key) { return props.getString(key, null); }
	public String get(String key, String defaultValue) { return props.getString(key,defaultValue); }
}
