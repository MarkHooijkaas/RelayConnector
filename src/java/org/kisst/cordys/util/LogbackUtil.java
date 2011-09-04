package org.kisst.cordys.util;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class LogbackUtil {
	private static Logger log = LoggerFactory.getLogger(LogbackUtil.class);
	
	public static void configure(String filename, HashMap<String, String> logbackProps) {
	    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
	    try {
	      JoranConfigurator configurator = new JoranConfigurator();
	      configurator.setContext(lc);
	      lc.reset(); 
	      for (String key: logbackProps.keySet())
	    	  lc.putProperty(key, logbackProps.get(key));
	      configurator.doConfigure(filename);
	    } 
	    catch (JoranException je) { /* ignore: StatusPrinter will handle this */ }
	    StatusPrinter.printInCaseOfErrorsOrWarnings(lc);

		log.info("logback configured from file "+filename);
	}
}

