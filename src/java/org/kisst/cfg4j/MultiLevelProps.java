package org.kisst.cfg4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class MultiLevelProps {
	private final HashMap<String,Props> props=new HashMap<String,Props>();
	private final LayeredProps globalProps=new LayeredProps(null);

	public MultiLevelProps(InputStream configStream) {
		load(configStream);
	}
	public Props getGlobalProps() { return globalProps; }
	public Props getProps(String key) {
		Props result = props.get(key);
		if (result==null)
			return globalProps;
		else
			return result;
	}

	/*
	private void load(String filename)  {
		props.clear();
		FileInputStream inp = null;
		try {
			try {
				inp = new FileInputStream(filename);
				load(inp);
			}
			finally {
				if (inp!=null) inp.close();
			}
		}
		catch (IOException e) { throw new RuntimeException(e); }
	}
	*/

	private void load(InputStream inp)  {
		if (inp==null)
			return;
		load(globalProps, new BufferedReader(new InputStreamReader(inp)));
	}

	private void load(LayeredProps props, BufferedReader input)  {
		try {
			String str;
			while ((str = input.readLine()) != null) {
				str=str.trim();
				if (str.startsWith("#") || str.length()==0) {
					//ignore comments and empty lines
				}
				else if (str.equals("}")) 
					return;
				else if (str.startsWith("@override")) {
					if (!str.endsWith("{"))
						throw new RuntimeException("override should have { symbol on same line in config line: "+str);
					String key=str.substring(9,str.length()-1).trim();
					LayeredProps subprops=new LayeredProps(props);
					this.props.put(key, subprops);
					load(subprops, input);
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
	}
}
