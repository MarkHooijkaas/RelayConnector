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

package org.kisst.cordys.as400.conn;

import org.kisst.cordys.as400.As400Configuration;

public class PoolConfiguration {
	private final String name;
	private final String host;
	private final String user;
	private final String password;
	private final int connectionPoolSize;
	private final long maxConnectionLifetimeMillis;
	private final long minEvictableIdleTimeMillis;
	private final long timeBetweenEvictionRunsMillis;
	
	private final boolean simulationFlag;
	private final int socketTimeout;
	private final int nrOfMessagesToLog;


	public PoolConfiguration(final String name, As400Configuration conf) {
		this.name = name;
		String prefix="as400.pool."+name;
		host = conf.getStringValue(prefix+".system", null);
		user = conf.getStringValue(prefix+".user", conf.getAs400user());
		password = conf.getStringValue(prefix+".password", conf.getAs400password());
		
		nrOfMessagesToLog  = conf.getIntValue(prefix+".nrOfMessagesToLog", 10);
		connectionPoolSize = conf.getIntValue(prefix+".maxSize", 10);
		maxConnectionLifetimeMillis = conf.getLongValue(prefix+".maxConnectionLifetimeMillis", 1800000);
		minEvictableIdleTimeMillis = conf.getLongValue(prefix+".minEvictableIdleTimeMillis", 300000);
		timeBetweenEvictionRunsMillis = conf.getLongValue(prefix+".timeBetweenEvictionRunsMillis", 60000);
		
		simulationFlag = conf.getBooleanValue(prefix+".simulationFlag", false);
		socketTimeout = conf.getIntValue(prefix+".socketTimeout", 0);
	}
	
	public String getHost() { return host; }
	public String getUser() { return user;	}
	public int getConnectionPoolSize() { return connectionPoolSize;	}
	public int getNrOfMessagesToLog() { return nrOfMessagesToLog;	}
	public long getMaxConnectionLifetimeMillis() { return maxConnectionLifetimeMillis; }
	public long getMinEvictableIdleTimeMillis() { return minEvictableIdleTimeMillis; }
	public long getTimeBetweenEvictionRunsMillis() { return timeBetweenEvictionRunsMillis; }
	public String getPassword() { return password; }
	public String getName() { return name;	}
	public boolean getSimulationFlag() { return simulationFlag;	}
	public int getSocketTimeout() { return socketTimeout; }
}
