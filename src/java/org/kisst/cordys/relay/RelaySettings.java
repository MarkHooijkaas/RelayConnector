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

package org.kisst.cordys.relay;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cfg4j.MappedSetting;
import org.kisst.cfg4j.StringSetting;
import org.kisst.cordys.relay.resourcepool.ResourcePoolSettings;
import org.kisst.cordys.util.SeveritySetting;

import com.eibus.util.logger.Severity;

public class RelaySettings {
	private final static CompositeSetting relay=new CompositeSetting(null,"relay");

	public final static IntSetting timeout=new IntSetting(relay, "timeout",20000);
	public final static BooleanSetting cacheScripts=new BooleanSetting(relay, "cacheScripts", false);

	public final static StringSetting soapFaultcodePrefix=new StringSetting(relay, "soapFaultcodePrefix", "TECHERR.ESB.");
	public final static SeveritySetting logRelayedSoapFaults=new SeveritySetting(relay, "logRelayedSoapFaults",Severity.WARN);
	public final static SeveritySetting trace=new SeveritySetting(relay, "trace",Severity.WARN);
	public final static BooleanSetting logTrace=new BooleanSetting(relay, "logTrace",true);
	public final static BooleanSetting showTrace=new BooleanSetting(relay, "showTrace",false);
	public final static BooleanSetting showStacktrace=new BooleanSetting(relay, "showStacktrace",false);
	public final static BooleanSetting traceShowEnvelope=new BooleanSetting(relay, "traceShowEnvelope",false);
	public final static BooleanSetting logRequestOnError=new BooleanSetting(relay, "logRequestOnError",true);

	public final static BooleanSetting timer=new BooleanSetting(relay, "timer",false);

	public final static BooleanSetting forbidden=new BooleanSetting(relay, "forbidden",false);
	public final static IntSetting sleepAfterCall=new IntSetting(relay, "sleepAfterCall",0);
	
	public final static MappedSetting<ResourcePoolSettings> resourcepool=
		new MappedSetting<ResourcePoolSettings>(relay, "resourcepool", ResourcePoolSettings.class);;

}
