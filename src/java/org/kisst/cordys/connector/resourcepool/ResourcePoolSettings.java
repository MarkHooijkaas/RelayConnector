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

package org.kisst.cordys.connector.resourcepool;

import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.StringSetting;

import org.kisst.cordys.relay.RelayTrace;
import org.kisst.cordys.util.ReflectionUtil;

import com.eibus.util.logger.Severity;

public class ResourcePoolSettings extends CompositeSetting {
	public final StringSetting className = new StringSetting(this, "class", null); // TODO: mandatory?; 
	public final IntSetting max = new IntSetting(this, "max", -1);  


	public ResourcePoolSettings(CompositeSetting parent, String name) { 
		super(parent, name); 
	}


	public ResourcePool create(Props props) {
		if (! className.exists(props))
			return createSimplePool(props);
		String classname=className.get(props);
		if (classname.indexOf(".")<0)
			classname="org.kisst.cordys.relay.resourcepool."+classname;
		return (ResourcePool) ReflectionUtil.createObject(classname, 
				new Object[]{this, props});
	}
	public ResourcePool createSimplePool(Props props) {
		if (max.exists(props))
			return new SimpleResourcePool(name,max.get(props));
		else {
			RelayTrace.logger.log(Severity.WARN, "Property "+max.getFullName()+" is not defined");
			return null;
		}
	}
	
}