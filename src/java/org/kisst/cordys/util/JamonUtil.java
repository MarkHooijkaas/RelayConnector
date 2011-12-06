package org.kisst.cordys.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.dbcp.BasicDataSource;
import org.kisst.cordys.connector.BaseConnector;
import org.kisst.cordys.connector.BaseSettings;
import org.kisst.props4j.Props;
import org.kisst.util.DatabaseUtil;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorComposite;
import com.jamonapi.MonitorFactory;

public class JamonUtil {
	public static void reset(BaseConnector connector) {
		initFileLogging(connector);
		initDatabase(connector.getProps());
	}
	
	private static Comparator<Monitor> comparator = new Comparator<Monitor>() {
		public int compare(Monitor o1, Monitor o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getLabel(), o2.getLabel());
		}
	};

	private static void logAndResetAllTimers(String filename, String message) {
		MonitorComposite rootMon = MonitorFactory.getRootMonitor();
		Monitor[] monitors = null;
		if (rootMon!=null) 
			monitors=rootMon.getMonitors();
		if (monitors!=null) {
			Arrays.sort(monitors,comparator);
			logToFile(message, monitors);
			logToDatabase(monitors);
			for (Monitor mon : monitors)
				mon.reset();
		}
	}

	private static String filename=null;
	@SuppressWarnings("deprecation")
	private static void initFileLogging(BaseConnector connector) {
		Props props = connector.getProps();
		if (! BaseSettings.jamon.logfile.enabled.get(props)) {
			filename=null;
			return;
		}
		String filename=BaseSettings.jamon.logfile.filename.get(props);
		filename = filename.replace("${cordys.home}", System.getProperty("cordys.home").replaceAll("[\\\\]", "/"));
		filename = filename.replace("${processor}", DnUtil.getFirstDnPart(connector.getProcessorName()));
		filename = filename.replace("${org}", DnUtil.getFirstDnPart(connector.getDnOrganization()));
		Date now = new Date();
		filename = filename.replace("${yyyy}", ""+(now.getYear()+1900));
		filename = filename.replace("${mm}", ""+(now.getMonth()+1));
		filename = filename.replace("${dd}", ""+(now.getDate()));
		filename = filename.replace("${dollar}", "$");
		// Note: the following action is to prevent that a partially built filename is used by the Jamon logging thread,
		// while the static filename is being recalculated by a reset thread. 
		// This is not 100% Thread safe, but should be quite atomic 
		JamonUtil.filename = filename;
	}

	private static void logToFile(String message, Monitor[] monitors) {
		if (filename==null)
			return;
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		StringBuilder msg= new StringBuilder();
		msg.append(format.format(new Date()));
		msg.append('\t');
		msg.append(message);
		msg.append('\n');
		for (Monitor mon : monitors){
			if (mon.getHits()==0)
				continue;
			msg.append(format.format(new Date()));
			msg.append('\t');
			msg.append(mon);
			msg.append('\n');
		}
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filename, true);
			out.write(msg.toString().getBytes());
		} catch (IOException e) { throw new RuntimeException(e);}
		finally {
			if (out!=null) {
				try { out.close(); }
				catch (IOException e) { throw new RuntimeException(e);}
			}
		}
	}

	public static void jamonLog(BaseConnector connector, String message) {
		Props props = connector.getProps();
		if (props==null) {
			//logger.warn("Could not perform jamon logging because properties are not available for jamonLog: "+message);
			return;
		}
		if (! BaseSettings.jamon.enabled.get(props))
			return;
		JamonUtil.logAndResetAllTimers(filename, message);
	}


	public static class JamonThread implements Runnable {
		private final BaseConnector connector;
		private boolean running=true;
		private Thread myThread=null;
		public JamonThread(BaseConnector connector) {
			this.connector = connector;
		} 
		public void run() {
			myThread = Thread.currentThread();
			while (running) {
				int interval = BaseSettings.jamon.intervalInSeconds.get(connector.getProps());
				try {
					if (interval<=0)
						Thread.sleep(600*1000); // sleep 10 minutes, could be any time
					else
						Thread.sleep(interval*1000);
					synchronized(this) {
						if (interval>0)
							jamonLog(connector, "TIMER expired after "+interval+" seconds, dumping all statistics");
					}
				} catch (InterruptedException e) { /* ignore, probably a reset or stop */  }
			}
			myThread=null;
		}
		public synchronized void stop() {
			running=false;
			reset();
		}
		public synchronized void  reset() {
			if (myThread!=null)
				myThread.interrupt();
		}
	}


	private static BasicDataSource ds=null;

	private static void initDatabase(Props props) {
		if ( BaseSettings.jamon.db.enabled.get(props)) 
			ds=DatabaseUtil.createDataSource(BaseSettings.jamon.db, props);
		else
			ds=null;
	} 	

	private static String serverIP;
	private static String sql = "INSERT INTO JFMONENTRY (SERVER, FUNCTION, TYPE, HITS, AVERAGERESPONSETIME, MAXIMUMRESPONSETIME, MINIMUMRESPONSETIME, DEVIATION, REGISTRATIONMOMENT)"
		+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private static void logOneEntry(Connection conn, Monitor mon) throws SQLException {
		if (serverIP==null) {
			// Note: this is not thread safe, but this should only be called from the jamon thread
			try {
				serverIP = java.net.InetAddress.getLocalHost().toString();
			}
			catch (UnknownHostException e) { serverIP="unknown"; }
		}
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sql);
			String[] parts = mon.getLabel().split("[:]");
			String type=parts[0];
			String function=parts[0];
			if (parts.length>1)
				function=parts[1];
			stmt.setString(1,serverIP);
			stmt.setString(2,function); 
			stmt.setString(3,type); 
			stmt.setDouble(4,mon.getHits());
			stmt.setDouble(5,mon.getAvg());
			stmt.setDouble(6,mon.getMax());
			stmt.setDouble(7,mon.getMin());
			stmt.setDouble(8,mon.getStdDev());
			stmt.setDate(9, new java.sql.Date(System.currentTimeMillis()));

			int result = stmt.executeUpdate();
			if (result!=1) {
				// ignore, maybe should be logged
				//throw new RuntimeException("Insert statement should have returned exactly one result "+sql);
			}
		}
		finally {
			if (stmt!=null)
				stmt.close();
		}
	}
	private static void logToDatabase(Monitor[] monitors) {
		if (ds==null)
			return;
		Connection conn=null; 
		try {
			conn = ds.getConnection();
			for (Monitor mon : monitors){
				if (mon.getHits()==0)
					continue;
				logOneEntry(conn, mon);
			}
		} 
		catch (SQLException e) { throw new RuntimeException(e); }
		finally {
			if (conn!=null)
				try {
					conn.close();
				} catch (SQLException e) { throw new RuntimeException(e); }
		}
	}
}

