package org.kisst.cordys.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorComposite;
import com.jamonapi.MonitorFactory;

public class JamonUtil {
	private static Comparator<Monitor> comparator = new Comparator<Monitor>() {
		public int compare(Monitor o1, Monitor o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getLabel(), o2.getLabel());
		}
	};
	
	public static void logAndResetAllTimers(String filename, String message) {
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		StringBuilder msg= new StringBuilder();
		msg.append(format.format(new Date()));
        msg.append('\t');
		msg.append(message);
        msg.append('\n');
        MonitorComposite rootMon = MonitorFactory.getRootMonitor();
        Monitor[] monitors = null;
        if (rootMon!=null) 
        	monitors=rootMon.getMonitors();
        if (monitors!=null) {
        	Arrays.sort(monitors,comparator);
        	for (Monitor mon : monitors){
        		if (mon.getHits()==0)
        			continue;
        		msg.append(format.format(new Date()));
        		msg.append('\t');
        		msg.append(mon);
        		msg.append('\n');
        		mon.reset();
        	}
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
}

