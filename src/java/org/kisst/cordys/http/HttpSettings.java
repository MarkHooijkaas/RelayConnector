package org.kisst.cordys.http;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.MappedSetting;
import org.kisst.cfg4j.MultiSetting;
import org.kisst.cfg4j.StringSetting;

public class HttpSettings extends MultiSetting {
	public final MappedSetting<HostSettings> host=new MappedSetting<HostSettings>(this, "host", HostSettings.class);
	public final BooleanSetting  ignoreReturnCode=new BooleanSetting(this, "ignoreReturnCode", false);
	public final StringSetting   wireLogging=new StringSetting(this, "wireLogging", null);
	
	public HttpSettings(MultiSetting parent) { 
		super(parent, "http");
		//addRemainingFields();
	}

}
