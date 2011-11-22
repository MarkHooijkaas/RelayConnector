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

package org.kisst.cordys.connector;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;

import org.kisst.cordys.util.NomPath;
import org.kisst.cordys.util.NomUtil;
import org.kisst.props4j.Props;

import com.eibus.connector.nom.CancelRequestException;
import com.eibus.connector.nom.Connector;
import com.eibus.connector.nom.SOAPMessageListener;
import com.eibus.exception.ExceptionGroup;
import com.eibus.exception.TimeoutException;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

public class MethodCache {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(MethodCache.class);
	private static class CacheInfo {
		public final Cache cache;
		public final NomPath keypath;
		public CacheInfo(String path, Cache cache) {
			this.cache=cache;
			// compiler argument is null, so no prefixes are yet supported
			this.keypath=new NomPath(null,path);
		}
	}

	private final Document doc=new Document(); 
	private final HashMap<String, CacheInfo> caches=new HashMap<String, CacheInfo>(); 
	private CacheManager manager=null;
	private Connector connector=null;
	
	public void init(Connector connector2, Props props) {
		if (connector==null) {
			connector= connector2; 
		}
		String filename=props.getString("relay.cachemanager.file",null);
		String url=props.getString("relay.cachemanager.url",null);
		String cacheList = props.getString("relay.caches", null);
		if (filename!=null)
			manager = new CacheManager(filename);
		else if (url!=null) {
			try {
				manager = new CacheManager(new URL(url));
			}
			catch (CacheException e) { throw new RuntimeException(e); }
			catch (MalformedURLException e) { throw new RuntimeException(e); }
		}
		else if (cacheList !=null)
			manager=getDefaultManager();
		else
			 manager=null;
		if (manager!=null) {
			fillUserDefinedCaches(props);
			mapMethods(props);
		}
	}

	public void reset(Props props) {
		destroy();
		init(connector, props);
	}

	public void destroy() {
		if (manager!=null) {
			manager.removalAll();
			caches.clear();
			manager.shutdown();
			manager=null;
		}
	}    

	public void putResponse(int request, int response) {
		if (manager==null)
			return;
		String name=NomUtil.getUniversalName(request);
		CacheInfo info=caches.get(name);
		if (info==null)
			return;
		String key=Node.getData(info.keypath.findNode(request));
		info.cache.put(new Element(key,Node.writeToString(response, false)));
	}
	
	public int getResponse(int request) {
		if (manager==null)
			return 0;
		String name=NomUtil.getUniversalName(request);
		CacheInfo info=caches.get(name);
		if (info==null)
			return 0;
		String key=Node.getData(info.keypath.findNode(request));
		Element elm=info.cache.get(key);
		if (elm==null)
			return 0;
		String response = (String) elm.getValue();
		try {
			return doc.parseString(response);
		}
		catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
		catch (XMLException e) { throw new RuntimeException(e); }
	}
	
	
	public int sendAndWait(int method, long timeout) {
		int response=0;
		if (manager!=null)
			response=getResponse(method);
		try {
			if (response==0) {
				response = connector.sendAndWait(Node.getRoot(method),timeout);
				if (response!=0)
					putResponse(method, response);
			}
			return response;
		}
		catch (CancelRequestException e) {throw new RuntimeException(e); }
		catch (TimeoutException e) {throw new RuntimeException(e); }
		catch (ExceptionGroup e) { throw new RuntimeException(e); }
	}
		
	@SuppressWarnings("deprecation")
	public void sendAndCallback(final int method, final SOAPMessageListener callback) {
		if (manager!=null) {
			int response=0;
			response=getResponse(method);
			if (response!=0) {
				callback.onReceive(response);
				return;
			}
		}
		try {
			connector.sendAndCallback(method,new SOAPMessageListener() {
				public boolean onReceive(int message) {
					putResponse(method, message);
					return callback.onReceive(message); // Node should not yet be destroyed by Callback caller!!
				}
			});
		} catch (ExceptionGroup e) {throw new RuntimeException(e); }
	}
	
	private CacheManager getDefaultManager() {
		CacheConfiguration cc=new CacheConfiguration();
		cc.setMaxElementsInMemory(1000);
		cc.setDiskPersistent(false);
		cc.setEternal(false);
		cc.setMemoryStoreEvictionPolicy("LRU");
		cc.setName("default");
		cc.setTimeToIdleSeconds(300);
		cc.setTimeToLiveSeconds(300);
		Configuration conf=new Configuration();
		conf.addDefaultCache(cc);
		return new CacheManager(conf);
	}

	private void fillUserDefinedCaches(Props props) {
		String cacheList = props.getString("relay.caches", null);
		if (cacheList==null)
			return;
		String[] cacheNames=cacheList.split(",");
		for (int i=0; i<cacheNames.length; i++) {
			String name=cacheNames[i].trim();
			String prefix="relay.cache."+name;
			int maxSize=props.getInt(prefix+".size", -1);
			long seconds=props.getLong(prefix+".timeToLiveSeconds", -1);
			Cache memoryOnlyCache = new Cache(name, maxSize, false, false, seconds, seconds);
			manager.addCache(memoryOnlyCache);
			//Cache test = singletonManager.getCache("testCache");
		}
	}

	private void mapMethods(Props props) {
		String[] cacheNames=manager.getCacheNames();
		for (int i=0; i<cacheNames.length; i++) {
			String name=cacheNames[i].trim();
			String prefix="relay.cache."+name;
			String methodName=props.getString(prefix+".method", null);
			if (methodName!=null) {
				String keypath=props.getString(prefix+".keypath", null);
				caches.put(methodName, new CacheInfo(keypath,manager.getCache(name)));
			}
			else {
				logger.log(Severity.WARN, "Could not find method info about cache "+name);
			}
		}
	}
}