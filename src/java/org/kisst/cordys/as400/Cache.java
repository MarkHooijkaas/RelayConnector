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

package org.kisst.cordys.as400;

import java.util.Date;
import java.util.HashMap;

import com.eibus.management.IManagedComponent;
import com.eibus.management.ISettingsCollection;
import com.eibus.management.OperationImpact;

public class Cache<K,V> {
	private final HashMap<K,V> map=new HashMap<K,V>();

	private Date firstModificationTime=null; 
	private Date expirationTime=null; 
	private int autoRefreshInterval=300;
	
	public void initJmx(IManagedComponent parent) {
		IManagedComponent mc = parent.createSubComponent("CacheType", "Cache", 
				new Message("Een simpel caching mechanisme"), this);
		ISettingsCollection s =  mc.getSettingsCollection();
		s.defineHotSetting("autoRefreshInterval", new Message("interval waarna hele cache leeggegooid wordt (0=geen caching, -1=oneindig)"), 
				"autoRefreshInterval",	this, null,	new Integer(300));
		mc.createPropertyBasedValueCounter("numberOfEntries", new Message("currently number of cached items"), "numberOfEntries", this);
		mc.defineOperation("clear", new Message("clear the cache"), 
				"clear", this, OperationImpact.ACTION); //, new IParameterDefinition[]{});
	}
	
	public int getAutoRefreshInterval() {
		return autoRefreshInterval;
	}

	public int getNumberOfEntries() {	return map.size();	}
	
	public void setAutoRefreshInterval(int autoRefreshInterval) {
		this.autoRefreshInterval = autoRefreshInterval;
		if (autoRefreshInterval < 0 ) {
			expirationTime=null;
		}
		else if (firstModificationTime!=null) {
			expirationTime=new Date(firstModificationTime.getTime()+autoRefreshInterval);
		}
	}


	private void expireIfNecessary() {
		if (autoRefreshInterval<0)
			return; // a negative interval means that cache will never expire
		if (expirationTime==null)
			return; // cacheshould be empty
		if (expirationTime.compareTo(new Date())<0) {
			clear();
			firstModificationTime=null;
		}
	}
	private void setModificationTime() {
		if (autoRefreshInterval<0)
			return;
		if (firstModificationTime==null) {
			firstModificationTime=new Date();
			expirationTime=new Date(firstModificationTime.getTime()+autoRefreshInterval*1000);
		}
	}
	
	
	public synchronized void clear() {
		map.clear();
		expirationTime = null;
		firstModificationTime = null;
	}
	public synchronized void put(K key, V value) {
		if (autoRefreshInterval==0)
			return; // refresh is immediate, so no use to remember this value

		// if cache is too old, clear it first, before somthing is put in
		// otherwise the cached value will be cleared anyway next time
		expireIfNecessary();
		setModificationTime();
		map.put(key, value);
	}

	public synchronized V get(K key) {
		if (autoRefreshInterval==0)
			return null; // refresh is immediate, so should be expired anyway
		expireIfNecessary();
		return map.get(key);
	}
}
