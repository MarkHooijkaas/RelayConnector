package org.kisst.cfg4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyLoader {
	public static Properties loadProperties(String filename)  {
		try {
			Properties props=new Properties();
//			logger.info("loading settings from file "+filename);
			FileInputStream inp = null;
			try {
				inp = new FileInputStream(filename);
				props.load(inp);
			}
			finally {
				if (inp!=null) inp.close();
			}
			return props;
		}
		catch (IOException e) { throw new RuntimeException(e); }
	}
}
