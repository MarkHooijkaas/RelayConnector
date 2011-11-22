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

package org.kisst.cordys.as400;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cfg4j.LongSetting;
import org.kisst.cfg4j.StringSetting;

public class As400PoolSettings extends CompositeSetting {
	//private final String name;

	public final StringSetting host = new StringSetting(this, "host");  
	public final StringSetting username = new StringSetting(this, "username");  
	public final StringSetting password = new StringSetting(this, "password");
	public final LongSetting timeout = new LongSetting(this, "timeout", 20000);

	public final IntSetting maxSize = new IntSetting(this, "maxSize", 10);
	public final IntSetting maxIdle = new IntSetting(this, "maxIdle", 8);
	public final IntSetting minIdle = new IntSetting(this, "minIdle", 0);
	public final LongSetting maxWait = new LongSetting(this, "maxWait", 20000);
	public final LongSetting maxConnectionLifetimeMillis= new LongSetting(this, "maxConnectionLifetimeMillis", 1800000);
	public final LongSetting minEvictableIdleTimeMillis= new LongSetting(this, "minEvictableIdleTimeMillis", 300000);
	public final LongSetting softMinEvictableIdleTimeMillis= new LongSetting(this, "softMinEvictableIdleTimeMillis", 300000);
	public final LongSetting timeBetweenEvictionRunsMillis= new LongSetting(this, "timeBetweenEvictionRunsMillis", 60000);
	public final IntSetting numTestsPerEvictionRun = new IntSetting(this, "numTestsPerEvictionRun", 3);
	public final BooleanSetting lifo = new BooleanSetting(this, "lifo", true);
	
	public final BooleanSetting simulationFlag = new BooleanSetting(this, "simulationFlag", false);
	public final IntSetting nrOfMessagesToLog = new IntSetting(this, "nrOfMessagesToLog", 10);


	public As400PoolSettings(CompositeSetting parent, String name) { 
		super(parent, name); 
	}
	
}