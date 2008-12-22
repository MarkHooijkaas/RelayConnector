package org.kisst.cordys.relay;


import java.io.FileInputStream;
import java.util.Properties;

import org.kisst.cfg4j.MultiSetting;

import com.cordys.coe.exception.GeneralException;
import com.cordys.coe.util.XMLProperties;

public class RelayConfiguration 
{
	//private static final CordysLogger logger = CordysLogger.getCordysLogger(RelayConfiguration.class);
	
	private static final String PROP_CONFIG_LOCATION = "ConfigLocation";

	public final RelaySettings settings;
	private String configLocation;
	public final Properties properties = new Properties();

	public RelayConfiguration(MultiSetting parent) {
		settings=new RelaySettings(parent);
	}
	
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
		settings.set(properties);
	}
	
	public boolean getCacheScripts() {	return settings.cacheScripts.get(); }
	public long getTimeout() {	return settings.timeout.get(); }
	public String get(String key) { return properties.getProperty(key); }
	public String get(String key, String defaultValue) { return properties.getProperty(key,defaultValue); }
}
