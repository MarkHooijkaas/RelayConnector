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

import org.apache.commons.pool.impl.GenericObjectPool;
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

public class SmartPool implements As400ConnectionPool {

	private final GenericObjectPool pool=new GenericObjectPool();
	private final PoolConfiguration conf;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(SmartPool.class);	
	private final Scheduler scheduler;
	
	public SmartPool(PoolConfiguration conf) {
		StdSchedulerFactory factory = new StdSchedulerFactory();
		try {
			scheduler= factory.getScheduler();
			scheduler.start();
		} catch (SchedulerException e) { throw new RuntimeException(e); }
		this.conf=conf;	
		pool.setFactory(new As400ConnectionFactory(conf));
	}


	public void init() {
		pool.setMaxActive(conf.getConnectionPoolSize());
		pool.setMinEvictableIdleTimeMillis(conf.getMinEvictableIdleTimeMillis());
		pool.setTimeBetweenEvictionRunsMillis(conf.getTimeBetweenEvictionRunsMillis());
	}

	public As400Connection borrowConnection(long timeout) {
		// TODO: now the pool checks the lifetime only when borrowing a connections
		// so a connection can live much longer if it is not needed
		logger.log(Severity.DEBUG, "Starting borrowing connection");
		long notCreatedBefore=0;
		long lifetime = conf.getMaxConnectionLifetimeMillis();
		if (lifetime>0) 
			notCreatedBefore = new java.util.Date().getTime() - lifetime; 
		while (true) {
			try {
				logger.log(Severity.DEBUG, "Borrowing connection from pool(NumActive,MaxIdle,MaxWait) "+pool.getNumActive()+ "--"+pool.getMaxIdle()+ "--"+pool.getMaxWait());
				As400Connection conn = (As400Connection) pool.borrowObject();
				logger.log(Severity.DEBUG, "Borrowed a connection from pool(NumActive,MaxIdle,MaxWait) "+pool.getNumActive()+ "--"+pool.getMaxIdle()+ "--"+pool.getMaxWait());				
				if (conn.creationTime < notCreatedBefore)
					pool.invalidateObject(conn);
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
				pool.returnObject(conn);
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
				pool.invalidateObject(conn);
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
}
