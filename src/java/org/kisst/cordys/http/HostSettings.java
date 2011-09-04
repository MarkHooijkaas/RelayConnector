/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.kisst.cordys.http;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.StringSetting;
import org.kisst.props4j.Props;

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