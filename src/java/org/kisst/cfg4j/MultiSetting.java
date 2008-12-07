package org.kisst.cfg4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Properties;

//import org.apache.log4j.Logger;

public class MultiSetting extends Setting {
  //public static final Logger logger = Logger.getLogger(MultiSetting.class);

  public final ArrayList settings=new ArrayList();

  public MultiSetting(MultiSetting parent, String name) {
	super(parent, name);
  }

  public void set(Properties props, String parentName) {
	//logger.debug("Setting "+fullName);
	String path=parentName+name;
	String str=(String) props.get(path);
	if (str!=null && str.startsWith("@"))
		path=str.substring(1).trim();
	path+=".";
	for (int i=0; i<settings.size(); i++) {
	  Setting s=(Setting) settings.get(i);
	  //logger.debug("Setting "+s.fullName);
	  s.set(props, path);
	}
  }


  public String asString() { return "TODO"; }
  public void set(String value) { throw new RuntimeException("NOT Supported"); }

  public boolean ok() {
	for (int i=0; i<settings.size(); i++) {
	  Setting s=(Setting) settings.get(i);
	  if (! s.ok()) return false;
	}
	return true;
  }

  @SuppressWarnings("unchecked")
public void add(Setting s) {	
//	  logger.debug("registering "+s.fullName);
	  settings.add(s);  
  }


  public void load(String filename)  {
	set(loadProperties(filename));
  }

  public static Properties loadProperties(String filename)  {
	try {
	  Properties props=new Properties();
//	  logger.info("loading settings from file "+filename);
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

  public void addRemainingFields() {
	  try {
		  Field[] fields=this.getClass().getDeclaredFields();
		  for (int i=0; i<fields.length; i++) {
			  Field fld=fields[i];
			  if (Setting.class.isAssignableFrom(fld.getType())) {
				  // TODO: check if public
				  if (fld.get(this)!=null) {
					  Constructor cons=fld.getType().getConstructor(new Class[] {MultiSetting.class, String.class});
					  Object obj=cons.newInstance(new Object[] { this, fld.getName() });
					  fld.set(this, obj);
				  }
			  }
		  }
	  }
	  catch (Exception e) { throw new RuntimeException(e); } // TODO: more specific
  }
}
