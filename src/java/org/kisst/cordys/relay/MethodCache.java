package org.kisst.cordys.relay;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;

import org.kisst.cordys.util.NomPath;
import org.kisst.cordys.util.NomUtil;

import com.eibus.connector.nom.CancelRequestException;
import com.eibus.connector.nom.Connector;
import com.eibus.connector.nom.SOAPMessageListener;
import com.eibus.directory.soap.DirectoryException;
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
	
	public void init(Properties props) {
		if (connector==null) {
			try {
				connector= Connector.getInstance("CachedMethodCaller");
			} 
			catch (ExceptionGroup e) { throw new RuntimeException(e); }
			catch (DirectoryException e) { throw new RuntimeException(e); }
		}
		String filename=props.getProperty("relay.cache.manager.file");
		String url=props.getProperty("relay.cache.manager.url");
		String cacheList = props.getProperty("relay.caches");
		if (filename!=null)
			manager = new CacheManager(filename);
		else if (url!=null) {
			try {
				manager = new CacheManager(new URL(url));
			}
			catch (CacheException e) { throw new RuntimeException(e); }
			catch (MalformedURLException e) { throw new RuntimeException(e); }
		}
		if (cacheList !=null && manager==null)
			manager=getDefaultManager();
		fillUserDefinedCaches(props);
		mapMethods(props);
	}

	public void reset(Properties properties) {
		destroy();
		init(properties);
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
		try {
			int response = connector.sendAndWait(method,timeout);
			if (response!=0)
				putResponse(method, response);
			return response;
		}
		catch (CancelRequestException e) {throw new RuntimeException(e); }
		catch (TimeoutException e) {throw new RuntimeException(e); }
		catch (ExceptionGroup e) { throw new RuntimeException(e); }
	}
		
	public void sendAndCallback(final int method, final SOAPMessageListener callback) {
		try {
			connector.sendAndCallback(method,new SOAPMessageListener() {
				@SuppressWarnings("deprecation")
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

	private void fillUserDefinedCaches(Properties props) {
		String cacheList = props.getProperty("relay.caches");
		String[] cacheNames=cacheList.split(",");
		for (int i=0; i<cacheNames.length; i++) {
			String name=cacheNames[i].trim();
			String prefix="relay.cache."+name;
			int maxSize=Integer.parseInt(props.getProperty(prefix+".size"));
			long seconds=0;
			Cache memoryOnlyCache = new Cache(name, maxSize, false, false, seconds, seconds);
			manager.addCache(memoryOnlyCache);
			//Cache test = singletonManager.getCache("testCache");
		}
	}

	private void mapMethods(Properties props) {
		String[] cacheNames=manager.getCacheNames();
		for (int i=0; i<cacheNames.length; i++) {
			String name=cacheNames[i].trim();
			String prefix="relay.cache."+name;
			String methodName=props.getProperty(prefix+".method");
			if (methodName!=null) {
				String keypath=props.getProperty(prefix+".keypath");
				caches.put(methodName, new CacheInfo(keypath,manager.getCache(name)));
			}
			else {
				logger.log(Severity.WARN, "Could not find method info about cache "+name);
			}
		}
	}
}
