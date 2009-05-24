package org.kisst.cordys.http;

import java.util.Properties;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.MappedSetting;
import org.kisst.cfg4j.Setting;
import org.kisst.cfg4j.StringSetting;

public class HttpSettings extends CompositeSetting {
	public final MappedSetting<HostSettings> host;
	public final BooleanSetting  ignoreReturnCode;
	public final StringSetting   wireLogging;
	
	public HttpSettings(Setting parent, String name, Properties props) { 
		super(parent, name);
		host=new MappedSetting<HostSettings>(this, "host", props, HostSettings.class);
		ignoreReturnCode=new BooleanSetting(this, "ignoreReturnCode", props, false);
		wireLogging=new StringSetting(this, "wireLogging", props, null);
	}

}
