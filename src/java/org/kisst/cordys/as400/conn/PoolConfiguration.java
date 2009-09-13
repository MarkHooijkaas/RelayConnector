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
