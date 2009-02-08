package org.kisst.cordys.relay;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cfg4j.MultiSetting;
import org.kisst.cfg4j.StringSetting;

public class RelaySettings extends MultiSetting {
	public IntSetting timeout=new IntSetting(this, "timeout");
	public BooleanSetting cacheScripts=new BooleanSetting(this, "cacheScripts");
	public StringSetting logSoapFaults=new StringSetting(this, "logSoapFaults", null);

	public RelaySettings(MultiSetting parent) { super(parent, "relay"); }
}
