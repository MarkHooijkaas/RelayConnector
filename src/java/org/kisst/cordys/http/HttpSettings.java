package org.kisst.cordys.http;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cfg4j.MappedSetting;

public class HttpSettings {
	private final static CompositeSetting http=new CompositeSetting(null,"http");

	public final static MappedSetting<HostSettings> host=new MappedSetting<HostSettings>(http, "host", HostSettings.class);;
	public final static BooleanSetting  ignoreReturnCode=new BooleanSetting(http, "ignoreReturnCode", false);
	public final static IntSetting  timeout=new IntSetting(http, "timeout", 30000);
}
