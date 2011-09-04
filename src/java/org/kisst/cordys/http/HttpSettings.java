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

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cfg4j.MappedSetting;

public class HttpSettings {
	private final static CompositeSetting http=new CompositeSetting(null,"http");

	public final static MappedSetting<HostSettings> host=new MappedSetting<HostSettings>(http, "host", HostSettings.class);;
	public final static BooleanSetting  ignoreReturnCode=new BooleanSetting(http, "ignoreReturnCode", false);
	public final static IntSetting  timeout=new IntSetting(http, "timeout", 30000);
	public final static IntSetting  maxConnectionsPerHost=new IntSetting(http, "maxConnectionsPerHost", 100);
	public final static IntSetting  maxTotalConnections=new IntSetting(http, "maxTotalConnections", 100);
}
