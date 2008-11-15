package org.kisst.cordys.relay;


import java.io.FileInputStream;
import java.util.Properties;

import com.cordys.coe.exception.GeneralException;
import com.cordys.coe.util.XMLProperties;

public class RelayConfiguration 
{
	//private static final CordysLogger logger = CordysLogger.getCordysLogger(RelayConfiguration.class);
	
	private static final String PROP_CONFIG_LOCATION = "ConfigLocation";

	private String configLocation;
	private boolean cacheScripts;
	private long timeout;
	private final Properties properties = new Properties();

	
	public void init(int iConfigNode) 
	{
		//logger.debug("RelayConfiguration starting initialisation");
		XMLProperties xpBase;
		if (iConfigNode == 0)
			throw new RuntimeException("Configuration not found.");

		if (!com.eibus.xml.nom.Node.getName(iConfigNode).equals("configuration"))
			throw new RuntimeException("Root-tag of the configuration should be <configuration>");

		try {
			xpBase = new XMLProperties(iConfigNode);
		}
		catch (GeneralException e) { throw new RuntimeException(e); }
		configLocation     = xpBase.getStringValue(PROP_CONFIG_LOCATION); 
		
		load();
		//logger.debug("RelayConfiguration finished initialisation");
	}
	
	static private boolean getBooleanValue(Properties props, String key, boolean defaultValue) {
		String value = props.getProperty(key);
		if (value==null)
			return defaultValue;
		if (value.trim().length()==0)
			return defaultValue;
		if (value.trim().equals("true"))
			return true;
		if (value.trim().equals("false"))
			return false;
		throw new RuntimeException("boolean configuration value "+key+" should be true or false");
	}

	static private long getLongValue(Properties props, String key, long defaultValue) {
		String value = props.getProperty(key);
		if (value==null)
			return defaultValue;
		if (value.trim().length()==0)
			return defaultValue;
		return Long.parseLong(value);
	}

	
	public void load()
	{
		properties.clear();
		//logger.log(Severity.INFO,"(re)loading properties file: "+configLocation);
		if (configLocation==null || configLocation.trim().length()==0) {
			//logger.debug("configLocation not specified, clearing properties and skipping file");
		}
		else {
			FileInputStream inp = null;
			try {
				inp =new FileInputStream(configLocation);
				properties.load(inp);
			} 
			catch (java.io.IOException e) { throw new RuntimeException(e);  }
			finally {
				try {
					if (inp!=null) 
						inp.close();
				}
				catch (java.io.IOException e) { throw new RuntimeException(e);  }
			}
		}
		cacheScripts = getBooleanValue(properties, "script.cache", false);
		timeout=getLongValue(properties, "methodcall.timeout", 20000);
		//logger.debug("finished (re)loading properties file");
	}
	
	public String getConfigLocation() { return configLocation;}
	public boolean getCacheScripts() {	return cacheScripts; }
	public long getTimeout() {	return timeout; }
	public String get(String key) { return properties.getProperty(key); }
	public String get(String key, String defaultValue) { return properties.getProperty(key,defaultValue); }
}
