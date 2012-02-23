package org.kisst.util;

import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cfg4j.LongSetting;
import org.kisst.cfg4j.StringSetting;

public class DatabaseUtil {
	public static class Settings extends CompositeSetting {
		public Settings(CompositeSetting parent, String name) { super(parent, name); }
		public final StringSetting url = new StringSetting(this, "url", null); 
		public final StringSetting username = new StringSetting(this, "username", null);  
		public final StringSetting password = new StringSetting(this, "password", null);
		public final StringSetting driver = new StringSetting(this, "driver", "com.ibm.as400.access.AS400JDBCDriver");
		
		public final LongSetting minEvictableIdleTimeMillis = new LongSetting(this, "minEvictableIdleTimeMillis", 10*60*1000); 
		public final LongSetting timeBetweenEvictionRunsMillis =new LongSetting(this, "timeBetweenEvictionRunsMillis",20*60*1000);
		public final IntSetting numTestsPerEvictionRun=new IntSetting(this, "numTestsPerEvictionRun",8);

		public final IntSetting poolInitialSize=new IntSetting(this, "poolInitialSize",0);
		public final IntSetting poolMaxActive=new IntSetting(this, "poolMaxActive",8);
		public final IntSetting poolMaxIdle=new IntSetting(this, "poolMaxIdle",8);
		public final IntSetting poolMinIdle =new IntSetting(this, "poolMinIdle",0);
	}


	/*
	public static BasicDataSource createDataSource(Settings settings, Props props) {
		BasicDataSource ds = new BasicDataSource();

		ds.setMinEvictableIdleTimeMillis(settings.minEvictableIdleTimeMillis.get(props));
		ds.setTimeBetweenEvictionRunsMillis(settings.timeBetweenEvictionRunsMillis.get(props));
		ds.setNumTestsPerEvictionRun(settings.numTestsPerEvictionRun.get(props));

		ds.setInitialSize(settings.poolInitialSize.get(props));
		ds.setMaxActive(settings.poolMaxActive.get(props));
		ds.setMaxIdle(settings.poolMaxIdle.get(props));
		ds.setMinIdle(settings.poolMinIdle.get(props));

		ds.setTestOnBorrow(true);
		ds.setValidationQuery("SELECT 1 as tmp FROM JFMONENTRY");

		ds.setDriverClassName(settings.driver.get(props));
		ds.setUsername(settings.username.get(props));
		ds.setPassword(settings.password.get(props));
		ds.setUrl(settings.url.get(props));
		
		return ds;
	} 	
*/
}
