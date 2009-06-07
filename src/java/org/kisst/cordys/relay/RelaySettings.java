package org.kisst.cordys.relay;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cfg4j.StringSetting;

public class RelaySettings {
	private final static CompositeSetting relay=new CompositeSetting(null,"relay");

	public final static IntSetting timeout=new IntSetting(relay, "timeout",20000);
	public final static BooleanSetting cacheScripts=new BooleanSetting(relay, "cacheScripts", false);
	public final static StringSetting logSoapFaults=new StringSetting(relay, "logSoapFaults", null);
	public final static BooleanSetting showStacktrace=new BooleanSetting(relay, "showStacktrace",false);
}
