package org.kisst.cordys.http;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.StringSetting;

public class HostSettings extends CompositeSetting {
	public final StringSetting url = new StringSetting(this, "url", null); // TODO: mandatory?; 
	public final StringSetting username = new StringSetting(this, "username", null);  
	public final StringSetting password = new StringSetting(this, "password", null);
	public final StringSetting ntlmhost   = new StringSetting(this, "ntlmhost", null); // TODO: is this necessary 
	public final StringSetting ntlmdomain = new StringSetting(this, "ntlmdomain", null); 


	public HostSettings(CompositeSetting parent, String name) { 
		super(parent, name); 
	}
	
	public  Credentials getCredentials(Props props){
		if (ntlmdomain==null)
			return new UsernamePasswordCredentials(username.get(props), password.get(props));
		else
			return new NTCredentials(username.get(props), password.get(props), ntlmhost.get(props), ntlmdomain.get(props));
	}
}