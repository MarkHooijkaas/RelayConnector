package org.kisst.cordys.http;

import org.kisst.cfg4j.MultiSetting;
import org.kisst.cfg4j.StringSetting;

public class HostSettings extends MultiSetting {
	public final StringSetting url = new StringSetting(this, "url"); 
	public final StringSetting username = new StringSetting(this, "username"); 
	public final StringSetting password = new StringSetting(this, "password"); 
	
	
	public HostSettings(MultiSetting parent, String name) { super(parent, name); }
}
