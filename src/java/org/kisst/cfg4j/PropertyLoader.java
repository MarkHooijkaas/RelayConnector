package org.kisst.cfg4j;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PropertyLoader {
	public static Props loadProperties(String filename)  {
		try {
			Props props=new LayeredProps(null);
			FileInputStream inp = null;
			try {
				inp = new FileInputStream(filename);
				load(inp);
			}
			finally {
				if (inp!=null) inp.close();
			}
			return props;
		}
		catch (IOException e) { throw new RuntimeException(e); }
	}

	public static Props load(InputStream inp )  {
		BufferedReader input=null;
		LayeredProps props=new LayeredProps(null);
		try {
			input = new BufferedReader(new InputStreamReader(inp));
			String str;
			while ((str = input.readLine()) != null) {
				str=str.trim();
				if (str.startsWith("#") || str.length()==0) {
					//ignore comments and empty lines
				}
				else {
					int pos=str.indexOf('=');
					if (pos<0)
						throw new RuntimeException("props line should contain = sign "+str);
					String key=str.substring(0,pos).trim();
					String value=str.substring(pos+1).trim();
					props.put(key,value);
				}
			}
		}
		catch (IOException e) { throw new RuntimeException(e); }
		return props;
	}
}
