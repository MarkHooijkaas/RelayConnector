package org.kisst.cordys.as400;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.kisst.cordys.as400.conn.PoolConfiguration;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class As400Configuration 
{
	private static final CordysLogger logger = CordysLogger.getCordysLogger(As400Configuration.class);
	
	private static final String PROP_AS400_USER = "AS400user";
	private static final String PROP_AS400_PASSWORD = "AS400password";
	private static final String PROP_CONFIG_LOCATION = "ConfigLocation";

	private final HashMap<String, PoolConfiguration> poolConfigs = new HashMap<String, PoolConfiguration>() ;

	private String configLocation;
	private String[] poolNames;
	private Properties properties;

	private int ccsId;
	private long defaultTimeout;
		
	private String as400user;
	private String as400password;

	public void init(int iConfigNode) 
	{
		logger.debug("As400Configuration starting initialisation");
		if (iConfigNode == 0)
			throw new RuntimeException("Configuration not found.");

		if (!com.eibus.xml.nom.Node.getName(iConfigNode).equals("configuration"))
			throw new RuntimeException("Root-tag of the configuration should be <configuration>");

		int node=Node.getElement(iConfigNode, "Configuration");
		as400user          = Node.getData(Node.getElement(node, PROP_AS400_USER)); 
		as400password      = Node.getData(Node.getElement(node, PROP_AS400_PASSWORD)); 
		configLocation     = Node.getData(Node.getElement(node, PROP_CONFIG_LOCATION)); 
		
		reload();
		logger.debug("As400Configuration finished initialisation");
	}
	
	public void reload()
	{
		properties = new Properties();
		logger.log(Severity.INFO,"(re)loading properties file: "+configLocation);
		if (configLocation==null || configLocation.trim().length()==0) {
			logger.debug("configLocation not specified, clearing properties and skipping file");
			properties.clear();
		}
		else {
			InputStream inp = null;
			try {
				inp = getConfigStream();
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

		poolConfigs.clear();
		String poolList= getStringValue("as400.pools", "main");
		poolNames = poolList.split("[ \t]*,[ \t]*");
		for (int i=0; i<poolNames.length; i++) {
			String name=poolNames[i].trim();
			poolConfigs.put(name, new PoolConfiguration(name, this));
		}
		
		ccsId = getIntValue("as400.ccsid", 1140);
		defaultTimeout= getLongValue("as400.defaultTimeout", 20000);
		
		logger.debug("finished (re)loading properties file");
	}

	private InputStream getConfigStream() {
		try {
			return new FileInputStream(configLocation);
		} catch (FileNotFoundException e) { throw new RuntimeException(e); }
	}

	
	public String getStringValue(String key, String defaultValue) {
		String value = properties.getProperty(key);
		if (value==null)
			return defaultValue;
		if (value.trim().length()==0)
			return defaultValue;
		return value;
	}

	public int getIntValue(String key, int defaultValue) {
		String value = properties.getProperty(key);
		if (value==null)
			return defaultValue;
		if (value.trim().length()==0)
			return defaultValue;
		return Integer.parseInt(value);
	}

	public long getLongValue(String key, long defaultValue) {
		String value = properties.getProperty(key);
		if (value==null)
			return defaultValue;
		if (value.trim().length()==0)
			return defaultValue;
		return Long.parseLong(value);
	}

	public boolean getBooleanValue(String key, boolean defaultValue) {
		String value = properties.getProperty(key);
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
	
	public String getConfigLocation() { return configLocation;}
	public PoolConfiguration getPoolConfiguration(String name){ return poolConfigs.get(name); }
	public int getCcsId() {	return ccsId; }
	public String[] getPoolNames() {return poolNames; }
	
	public Properties getProperties() {
		return properties;
	}

	public String getAs400password() { return as400password; }
	public String getAs400user() { return as400user; }
	public long getDefaultTimeout() { return defaultTimeout;	}
}