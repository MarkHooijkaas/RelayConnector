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

package org.kisst.cordys.relay.resourcepool;

import org.kisst.cordys.relay.CallContext;


public class SimpleResourcePool implements ResourcePool {
	private int count=0;
	final private int max;
	final private String name;
	
	SimpleResourcePool(String name, int max) {
		this.name=name;
		this.max=max;
	}
	public synchronized void add(CallContext ctxt) {
		count++; // count isincreased here, because the remove method will be called even if exception is thrown
		if (count>max)
			throw new RuntimeException("ResourcePool "+name+" already contains "+max+" calls, aborting new call");
	}
	public synchronized void remove(CallContext ctxt) {
		count--;
	}
}
