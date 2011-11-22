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

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
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

	public final IntSetting maxTotal = new IntSetting(this, "maxTotal", GenericKeyedObjectPool.DEFAULT_MAX_TOTAL);
	public final IntSetting maxActive = new IntSetting(this, "maxActive", GenericKeyedObjectPool.DEFAULT_MAX_ACTIVE);
	public final IntSetting maxIdle = new IntSetting(this, "maxIdle", GenericKeyedObjectPool.DEFAULT_MAX_IDLE);
	public final IntSetting minIdle = new IntSetting(this, "minIdle", GenericKeyedObjectPool.DEFAULT_MIN_IDLE);
	public final LongSetting maxWait = new LongSetting(this, "maxWait", GenericKeyedObjectPool.DEFAULT_MAX_WAIT);
	public final LongSetting minEvictableIdleTimeMillis= new LongSetting(this, "minEvictableIdleTimeMillis", GenericKeyedObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS);
	public final LongSetting timeBetweenEvictionRunsMillis= new LongSetting(this, "timeBetweenEvictionRunsMillis", GenericKeyedObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS);
	public final IntSetting numTestsPerEvictionRun = new IntSetting(this, "numTestsPerEvictionRun", GenericKeyedObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN);
	public final BooleanSetting lifo = new BooleanSetting(this, "lifo", true);
	public final LongSetting maxConnectionLifetimeMillis= new LongSetting(this, "maxConnectionLifetimeMillis", 1800000);
	public final LongSetting maxNrofCallsPerConnection= new LongSetting(this, "maxNrofCallsPerConnection", -1);
	
	public final BooleanSetting simulationFlag = new BooleanSetting(this, "simulationFlag", false);
	public final IntSetting nrOfMessagesToLog = new IntSetting(this, "nrOfMessagesToLog", 10);


	public As400PoolSettings(CompositeSetting parent, String name) { 
		super(parent, name); 
	}
	
}