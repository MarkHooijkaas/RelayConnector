/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kisst.cordys.util;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cfg4j.StringSetting;
import org.kisst.util.DatabaseUtil;

public class JamonSettings extends CompositeSetting {
	public JamonSettings(CompositeSetting parent, String name) { super(parent, name); }

	public final BooleanSetting enabled=new BooleanSetting(this, "enabled",true);
	public final IntSetting intervalInSeconds=new IntSetting(this, "intervalInSeconds",300);
	public final DbSettings db =new DbSettings(this, "db");
	public final LogfileSettings logfile =new LogfileSettings(this, "logfile");

	
	
	public static class DbSettings extends DatabaseUtil.Settings {
		public DbSettings(CompositeSetting parent, String name) { super(parent, name); }
		public final BooleanSetting enabled=new BooleanSetting(this, "enabled",false);
	}
	
	public static class LogfileSettings extends CompositeSetting {
		public LogfileSettings(CompositeSetting parent, String name) { super(parent, name); }
		public final BooleanSetting enabled=new BooleanSetting(this, "enabled",false);
		public final StringSetting filename=new StringSetting(this, "filename", "${cordys.home}/Logs/jamon/${org}-${processor}-${yyyy}-${mm}-${dd}.log");
	}

}