package org.kisst.cordys.relay;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cordys.util.SeveritySetting;

import com.eibus.util.logger.Severity;

public class RelaySettings {
	private final static CompositeSetting relay=new CompositeSetting(null,"relay");

	public final static IntSetting timeout=new IntSetting(relay, "timeout",20000);
	public final static IntSetting sleepAfterCall=new IntSetting(relay, "sleepAfterCall",0);
	public final static BooleanSetting cacheScripts=new BooleanSetting(relay, "cacheScripts", false);
	public final static SeveritySetting logRelayedSoapFaults=new SeveritySetting(relay, "logRelayedSoapFaults",Severity.WARN);
	public final static BooleanSetting showStacktrace=new BooleanSetting(relay, "showStacktrace",false);
	public final static BooleanSetting timer=new BooleanSetting(relay, "timer",false);
	public final static BooleanSetting trace=new BooleanSetting(relay, "trace",false);;
}
