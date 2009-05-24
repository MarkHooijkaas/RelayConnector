package org.kisst.cfg4j;

import java.lang.reflect.Field;
import java.util.ArrayList;

//import org.apache.log4j.Logger;

public class CompositeSetting extends Setting {
	//public static final Logger logger = Logger.getLogger(MultiSetting.class);

	private final ArrayList<Setting> settings=new ArrayList<Setting>();

	public CompositeSetting(Setting parent, String name) {
		super(parent, name);
		addFields();
	}
	public String asString() { return "TODO"; }

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
}
