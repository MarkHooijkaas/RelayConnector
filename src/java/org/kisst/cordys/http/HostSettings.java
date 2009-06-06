package org.kisst.cordys.http;

import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.StringSetting;

public class HostSettings extends CompositeSetting {
	public final StringSetting url; 
	public final StringSetting username; 
	public final StringSetting password; 


	public HostSettings(CompositeSetting parent, String name) { 
		super(parent, name); 
		url = new StringSetting(this, "url", null); // TODO: mandatory? 
		username = new StringSetting(this, "username", null); 
		password = new StringSetting(this, "password", null); 
	}
}
