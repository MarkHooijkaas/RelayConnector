package org.kisst.cordys.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class JamonUtil {

	public static void logAndResetAllTimers(String filename) {
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		StringBuilder msg= new StringBuilder();
		for (Monitor mon:MonitorFactory.getRootMonitor().getMonitors()){
			msg.append(format.format(new Date()));
	        msg.append('\t');
	        msg.append(mon);
	        msg.append('\n');
	        mon.reset();
		}
        msg.append('\n');

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
}
