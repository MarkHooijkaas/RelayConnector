package org.kisst.cfg4j;

import java.util.ArrayList;

//import org.apache.log4j.Logger;

public class CompositeSetting extends Setting {
	private final ArrayList<Setting> settings=new ArrayList<Setting>();
	
	public CompositeSetting (String name) {
		super(null, name);
	}
	public CompositeSetting(CompositeSetting parent, String name) {
		super(parent, name);
	}
	public void add(Setting s) { settings.add(s); }
}
