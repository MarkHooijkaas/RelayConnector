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

import java.util.Date;
import java.util.HashSet;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.kisst.cordys.as400.As400PoolSettings;
import org.kisst.props4j.Props;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

public class As400ConnectionPool {
	private final As400PoolSettings settings;
	private final Props globalProps;
	private final HashSet<String> keys=new HashSet<String>();
	
	private final GenericKeyedObjectPool pool;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(As400ConnectionPool.class);	
	private final Scheduler scheduler;
	
	public As400ConnectionPool(As400PoolSettings settings, Props props) {
		this.settings=settings;
		this.globalProps=props;
		StdSchedulerFactory factory = new StdSchedulerFactory();
		try {
			scheduler= factory.getScheduler();
			scheduler.start();
		} catch (SchedulerException e) { throw new RuntimeException(e); }
		pool=new GenericKeyedObjectPool(new As400ConnectionFactory(settings, props));
	}


	public void init() {
		pool.setMaxTotal(settings.maxTotal.get(globalProps));
		pool.setMaxActive(settings.maxActive.get(globalProps) );
		pool.setMaxIdle(settings.maxIdle.get(globalProps));
		pool.setMaxWait(settings.maxWait.get(globalProps));
		pool.setMinIdle(settings.minIdle.get(globalProps));
		pool.setMinEvictableIdleTimeMillis(settings.minEvictableIdleTimeMillis.get(globalProps));
		pool.setTimeBetweenEvictionRunsMillis(settings.timeBetweenEvictionRunsMillis.get(globalProps));
		pool.setNumTestsPerEvictionRun(settings.numTestsPerEvictionRun.get(globalProps));
		pool.setLifo(settings.lifo.get(globalProps));
	}

	public As400Connection borrowConnection(Props callSpecificProps, String key) {
		long timeout=settings.timeout.get(callSpecificProps);
		synchronized (keys) {
			keys.add(key);
		}
		// TODO: now the pool checks the lifetime only when borrowing a connections
		// so a connection can live much longer if it is not needed
		logger.log(Severity.DEBUG, "Starting borrowing connection");
		long notCreatedBefore=0;
		long lifetime = settings.maxConnectionLifetimeMillis.get(globalProps);
		if (lifetime>0) 
			notCreatedBefore = new java.util.Date().getTime() - lifetime;
		while (true) {
			try {
				logger.log(Severity.DEBUG, "Borrowing connection from pool(NumActive,MaxIdle,MaxWait) "+pool.getNumActive()+ "--"+pool.getMaxIdle()+ "--"+pool.getMaxWait());
				As400Connection conn = (As400Connection) pool.borrowObject(key);
				logger.log(Severity.DEBUG, "Borrowed a connection from pool(NumActive,MaxIdle,MaxWait) "+pool.getNumActive()+ "--"+pool.getMaxIdle()+ "--"+pool.getMaxWait());				
				if (conn.creationTime < notCreatedBefore)
					pool.invalidateObject(conn, key);
				else {
					scheduleTrigger(conn, timeout);
					return conn;
				}
			}
			catch (Exception e) { throw new RuntimeException(e);}
		}
	}

	private static final String triggerGroup = "ThreadMonitor";
	private void scheduleTrigger(As400Connection conn, long timeout) {
		Date mytime=new Date(System.currentTimeMillis()+timeout);
		SimpleTrigger trigger = new SimpleTrigger(conn.getName(), triggerGroup, mytime, null, 0, 0L);
		JobDetail jobDetail = new JobDetail("kill "+conn.getName(), null, KillHangingCall.class);
		jobDetail.getJobDataMap().put("thread", Thread.currentThread());
		jobDetail.getJobDataMap().put("conn", conn);
		try {
			scheduler.scheduleJob(jobDetail , trigger);
		} catch (SchedulerException e) { throw new RuntimeException(e); }
	}
	public static class KillHangingCall implements Job {
		public void execute(JobExecutionContext ctx) throws JobExecutionException {
			As400Connection conn= (As400Connection) ctx.getJobDetail().getJobDataMap().get("conn");
			if (conn.killJob()) {
				return;
			}
			Thread t= (Thread) ctx.getJobDetail().getJobDataMap().get("thread");
			logger.log(Severity.ERROR, "Interrupting hanging thread on connection performing "+conn.getRunningProgam());
			t.interrupt();
			// TODO: Should we disconnect the connection, or do anything else?
			// The risk when calling a disconnect would be that this would hang this (monitor) thread
		}
	}


	public void releaseConnection(As400Connection conn) {
		try {
			scheduler.unscheduleJob(conn.getName(), triggerGroup);
		} catch (SchedulerException e) { throw new RuntimeException(e); }
		finally {
			try {
				long maxNrofCalls = settings.maxNrofCallsPerConnection.get(globalProps);
				if (maxNrofCalls>0 && conn.getNrOfCalls()>=maxNrofCalls)
					pool.invalidateObject(conn, conn.getPoolKey());
				else
					pool.returnObject(conn, conn.getPoolKey());
			} 
			catch (IllegalStateException e) {
				if ("Pool not open".equals(e.getMessage()))
					logger.log(Severity.WARN, "Ignoring [Pool not open] error, probably due to connector reset");
				else
					throw new RuntimeException(e); 
			}
			catch (Exception e) { throw new RuntimeException(e); }
		}
	}

	public void invalidateConnection(As400Connection conn) {
		try {
			scheduler.unscheduleJob(conn.getName(), triggerGroup);
		} catch (SchedulerException e) { throw new RuntimeException(e); }
		finally {
			try {
				pool.invalidateObject(conn, conn.getPoolKey());
			} 
			catch (IllegalStateException e) {
				if ("Pool not open".equals(e.getMessage()))
					logger.log(Severity.WARN, "Ignoring [Pool not open] error, probably due to connector reset");
				else
					throw new RuntimeException(e); 
			}
			catch (Exception e) { throw new RuntimeException(e); }
		}
	}

	public void reset() {
		try {
			pool.clear();
		}
		catch (Exception e) { throw new RuntimeException(e); }
	}

	public void destroy() {
		try {
			pool.close();
		} catch (Exception e) { throw new RuntimeException(e); }
	}
	
	public String status() {
		StringBuilder result = new StringBuilder();
		result.append("TOTAL\t");
		result.append(pool.getNumActive());
		result.append("\t");
		result.append(pool.getNumIdle());
		result.append("\n");
		
		for (String key: keys) {
			result.append(key);
			result.append("\t");
			result.append(pool.getNumActive(key));
			result.append("\t");
			result.append(pool.getNumIdle(key));
			result.append("\n");
		}
		
		return result.toString();
	}
}
