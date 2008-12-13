package org.kisst.cordys.relay;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cfg4j.MultiSetting;

public class RelaySettings extends MultiSetting {
	public IntSetting timeout=new IntSetting(this, "timeout");
	public BooleanSetting cacheScripts=new BooleanSetting(this, "cacheScripts");

	public RelaySettings(MultiSetting parent) { super(parent, "relay"); }
}
