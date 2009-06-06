package org.kisst.cordys.http;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.MappedSetting;
import org.kisst.cfg4j.Props;

public class HttpSettings extends CompositeSetting {
	public final MappedSetting<HostSettings> host=new MappedSetting<HostSettings>(this, "host", HostSettings.class);;
	public final BooleanSetting  ignoreReturnCode=new BooleanSetting(this, "ignoreReturnCode", false);
	
	public HttpSettings(Props props, String name) { 
		super(props, name);
	}

}
