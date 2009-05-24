package org.kisst.cordys.http;

import java.util.Properties;

import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.Setting;
import org.kisst.cfg4j.StringSetting;

public class HostSettings extends CompositeSetting {
	public final StringSetting url; 
	public final StringSetting username; 
	public final StringSetting password; 


	public HostSettings(Setting parent, String name, Properties props) { 
		super(parent, name); 
		url = new StringSetting(this, "url", props, null); // TODO: mandatory? 
		username = new StringSetting(this, "username", props, null); 
		password = new StringSetting(this, "password", props, null); 
	}
}
