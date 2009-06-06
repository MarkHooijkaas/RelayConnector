package org.kisst.cfg4j;

import java.util.ArrayList;
import java.util.Properties;

//import org.apache.log4j.Logger;

public class CompositeSetting extends Setting {
	//public static final Logger logger = Logger.getLogger(MultiSetting.class);

	private final ArrayList<Setting> settings=new ArrayList<Setting>();
	protected final Props props;
	
	public CompositeSetting (Props props, String name) {
		super(null, name);
		this.props=props;
	}
	public CompositeSetting(CompositeSetting parent, String name) {
		super(parent, name);
		this.props=parent.props;
		//addFields();
	}
	public String asString() { return "TODO"; }
	public void add(Setting s) { settings.add(s); }

	/*
	private void addFields() {
		Field[] fields=this.getClass().getDeclaredFields();
		for (int i=0; i<fields.length; i++) {
			Field fld=fields[i];
			if (Setting.class.isAssignableFrom(fld.getType())) {
				// TODO: check if public
				try {
					if (fld.get(this)!=null) {
						settings.add((Setting) fld.get(this));
					}
				} catch (IllegalAccessException e) { throw new RuntimeException(e); } 
			}
		}
	}
	*/
	public Properties getProperties() {
		if (parent==null)
			return null;
		else
			return parent.getProperties();
	}
}
