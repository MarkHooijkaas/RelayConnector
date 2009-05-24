package org.kisst.cfg4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Properties;

//import org.apache.log4j.Logger;

public class ExpandableSetting extends Setting {
	//public static final Logger logger = Logger.getLogger(MultiSetting.class);

	public final ArrayList settings=new ArrayList();

	public ExpandableSetting(Setting parent, String name, Properties props) {
		super(parent, name);
		addFields();
	}

	public String asString() { return "TODO"; }

	@SuppressWarnings("unchecked")
	public void add(Setting s) {	
//		logger.debug("registering "+s.fullName);
		settings.add(s);  
	}


	private void addFields() {
		Field[] fields=this.getClass().getDeclaredFields();
		for (int i=0; i<fields.length; i++) {
			Field fld=fields[i];
			if (Setting.class.isAssignableFrom(fld.getType())) {
				// TODO: check if public
				try {
					if (fld.get(this)!=null) {
						add((Setting) fld.get(this));
					}
				} catch (IllegalAccessException e) { throw new RuntimeException(e); } 
			}
		}
	}
}
