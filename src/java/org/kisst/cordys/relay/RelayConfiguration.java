package org.kisst.cordys.relay;


import java.io.FileInputStream;
import java.util.Properties;

public class RelayConfiguration 
{
	//private static final CordysLogger logger = CordysLogger.getCordysLogger(RelayConfiguration.class);

	
	public static Properties load(String configLocation)
	{
		Properties properties = new Properties();
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
		return properties;

	}
}
