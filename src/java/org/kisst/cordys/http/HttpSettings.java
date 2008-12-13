package org.kisst.cordys.http;

import java.util.HashMap;
import java.util.Properties;

import org.kisst.cfg4j.MultiSetting;

public class HttpSettings extends MultiSetting {
	private Properties properties;
	private final HashMap<String,HostSettings> hosts=new HashMap<String,HostSettings>();
	
	public HttpSettings(MultiSetting parent) { super(parent, null); }

	@Override
	public void set(Properties properties) {
		this.properties=properties;
		super.set(properties);
	}
	
	public HostSettings getHost(String name) {
		HostSettings result=hosts.get(name);
		if (result==null) {
			result=new HostSettings(this,name);
			result.set(properties);
			hosts.put(name, result);
		}
		return  result;
	}
}
