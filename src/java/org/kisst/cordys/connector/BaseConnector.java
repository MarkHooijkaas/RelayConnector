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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.kisst.cordys.connector.resourcepool.ResourcePool;
import org.kisst.cordys.connector.resourcepool.ResourcePoolSettings;
import org.kisst.cordys.script.ExecutionContext;
import org.kisst.cordys.util.DnUtil;
import org.kisst.cordys.util.JamonUtil;
import org.kisst.cordys.util.LogbackUtil;
import org.kisst.cordys.util.NomUtil;
import org.kisst.cordys.util.SoapUtil;
import org.kisst.props4j.Parser;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;

import com.eibus.connector.nom.CancelRequestException;
import com.eibus.connector.nom.Connector;
import com.eibus.directory.soap.DirectoryException;
import com.eibus.exception.ExceptionGroup;
import com.eibus.exception.TimeoutException;
import com.eibus.management.IManagedComponent;
import com.eibus.soap.ApplicationConnector;
import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.Processor;
import com.eibus.soap.SOAPTransaction;
import com.eibus.util.logger.CordysLogger;
import com.eibus.xml.nom.Node;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

abstract public class BaseConnector extends ApplicationConnector {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(BaseConnector.class);

	abstract protected String getConnectorName();
	abstract public ApplicationTransaction createTransaction(SOAPTransaction stTransaction);

	private HashMap<String,ResourcePool> resourcePoolMap=new HashMap<String,ResourcePool>();

	private Connector connector;
	private String configLocation;
	private String dnOrganization;
	private String processorName;
	private ArrayList<Module> modules=new ArrayList<Module>();
	protected Props props;
	private JamonUtil.JamonThread jamonThread;
	public final MethodCache responseCache=new MethodCache();

	public String getDnOrganization() { return dnOrganization; }
	public String getProcessorName() { return processorName; }

	
	/**
	 * This method gets called when the processor is started. It reads the
	 * configuration of the processor and creates the connector with the proper
	 * parameters.
	 * It will also create a client connection to Cordys.
	 *
	 * @param processor The processor that is started.
	 */
	@SuppressWarnings("deprecation")
	public void open(Processor processor)
	{
		dnOrganization =processor.getOrganization();
		processorName = processor.getSOAPProcessorEntry().getDN();
		try {
			initConfigLocation(getConfiguration());
			connector= Connector.getInstance(getConnectorName());
			if (!connector.isOpen())
				connector.open();

			InputStream stream = getConfigStream();
			if (stream==null)
				props=new SimpleProps();
			else {
				Parser parser = new Parser(stream);
				props = parser.readMap(null, null);
			}
			//CordysLogger specialLogger = CordysLogger.getCordysLogger(com.eibus.management.ManagedComponent.class);
			if (logger.isInfoEnabled())
				logger.info("starting with properties "+props);
			reconfigureLogback();
			init(getProps());
			addDynamicModules(getProps());
			for (int i=0; i<modules.size(); i++)
				modules.get(i).init(this);
		}
		catch (DirectoryException e) { throw new RuntimeException(e);	}
		catch (ExceptionGroup e) { throw new RuntimeException(e);	} 
		JamonUtil.jamonLog(this, "Starting Connector");
		jamonThread=new JamonUtil.JamonThread(this);
		Thread t = new Thread(jamonThread);
		t.setDaemon(true);
		t.start();
	}

	
	private void reconfigureLogback() {
	    HashMap<String, String> logbackProps = new HashMap<String,String>();
	    logbackProps.put("org", DnUtil.getFirstDnPart(getDnOrganization()).toLowerCase().replace(' ', '-'));
	    logbackProps.put("soapproc", DnUtil.getFirstDnPart(getProcessorName()).toLowerCase().replace(' ', '-'));

		String logbackConfigFile = getProps().getString("relay.logback.configFile", "D:/Cordys/kisst.org/config/logback.xml");
		if (new File(logbackConfigFile).isFile())
			LogbackUtil.configure(logbackConfigFile, logbackProps);
	}

	protected void init(Props globalProps) {}
	
	private void addDynamicModules(Props props) {
		String moduleList=(String) props.get("modules",null);
		if (moduleList!=null && moduleList.trim().length()>0) {
			String[] moduleNames=moduleList.split(",");
			for (int i=0; i<moduleNames.length; i++) {
				try {
					addModule((Module) Class.forName(moduleNames[i].trim()).newInstance());
				} catch (Exception e) {
					throw new RuntimeException("Could not load module class "+moduleNames[i]);
				}
			}
		}
	}

	protected void addModule(Module m) { modules.add(m); }
	public Module getModule(Class<?> cls) {
		for (Module m: modules)
			if (cls.isInstance(m))
				return m;
		throw new RuntimeException("Could not find a module of type "+cls.getSimpleName());
	}

	@Override
	public void reset(Processor processor) {
		reconfigureLogback();
		reset(); 
	}


	public void reset() {
		Parser parser = new Parser(getConfigStream());
		props = parser.readMap(null, "root");
		//mlprops =new MultiLevelProps(getConfigStream());
		for (int i=0; i<modules.size(); i++)
			modules.get(i).reset();
		JamonUtil.jamonLog(this,"RESET called, dumping all statistics");
		jamonThread.reset();
	}

	@Override
	public void close(Processor processor)
	{
		for (int i=0; i<modules.size(); i++)
			modules.get(i).destroy();
		JamonUtil.	jamonLog(this, "STOP called, dumping all statistics");
		jamonThread.stop();
	}    

	@Override
	protected IManagedComponent createManagedComponent() {
		IManagedComponent mc=super.createManagedComponent();
		return mc;
	}




	@Override protected String getManagedComponentType() { return getConnectorName(); }
	@Override protected String getManagementName() { return getConnectorName(); }

	public Connector getConnector() { return connector; }
	public String getOrganization() { return getDnOrganization(); }

	private void initConfigLocation(int configNode) 
	{
		logger.debug("RelayConfiguration starting initialisation");
		if (configNode == 0)
			throw new RuntimeException("Configuration not found.");

		if (!Node.getLocalName(configNode).equals("configuration"))
			throw new RuntimeException("Root-tag of the configuration should be <configuration>");
		// The old config.html stored the items under a Configuration node
		// The new config.html file does no longer do this
		// Check if the old location is still there, for SOAP processors created with the old
		// config.html
		int tmp=NomUtil.getElementByLocalName(configNode, "Configuration");
		if (tmp!=0)
			configNode=tmp;
		configLocation = Node.getData(NomUtil.getElementByLocalName(configNode,"ConfigLocation")); 
	}


	private InputStream getConfigStream() {
		if (configLocation==null || configLocation.trim().length()==0)
			return null;
		if (configLocation.startsWith("xmlstore:")) {
			return getConfigFileFromXmlStore();
		}
		else {
			try {
				return new FileInputStream(configLocation);
			} catch (FileNotFoundException e) { throw new RuntimeException(e); }
		}
	}	
	private InputStream getConfigFileFromXmlStore() {
		String location=configLocation.substring(9); // strip xmlstore: prefix
		String parts[]= location.split("[@]");
		if (parts.length !=2)
			throw new RuntimeException("Correct xmlstore configLocation format is xmlstore:dnUser@key");
		String dnUser=parts[0];
		String key=parts[1];
		String namespace = "http://schemas.cordys.com/1.0/xmlstore";
		String methodName="GetXMLObject";
		int method=0;
		int response=0;
		try {
			method=createMethod(namespace, methodName, dnUser);
			Node.createTextElement("key", key, method);
			try {
				response = connector.sendAndWait(Node.getRoot(method),20000);
			}
			catch (CancelRequestException e) {throw new RuntimeException(e); }
			catch (TimeoutException e) {throw new RuntimeException(e); }
			catch (ExceptionGroup e) { throw new RuntimeException(e); }
			
			if (SoapUtil.isSoapFault(response))
				throw new RuntimeException("Soap Fault while reading config form xmlstore "+SoapUtil.getSoapFaultMessage(response));
			int node=SoapUtil.getContent(response); 
			int confignode =NomUtil.getElementByLocalName(node, "tuple");
			confignode =NomUtil.getElementByLocalName(confignode, "old");
			if (confignode==0)
				throw new RuntimeException("Empty response (no tuple/old) while reading config form xmlstore "+SoapUtil.getSoapFaultMessage(response));
			String config = Node.getData(confignode);
			return new  java.io.ByteArrayInputStream(config.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
		finally {
			if (response!=0) Node.delete(response);
			if (method!=0) Node.delete(Node.getParent(Node.getParent(method)));

		}
	}
	public int createMethod(String namespace, String methodName, String dnUser) {
		final String marker="cn=organizational users,";
		String org=getDnOrganization();
		int pos= dnUser.indexOf(marker);
		// if the username is a fully qualified dn, use the organisation from this dn
		// otherwise add the connectors organization to the username 
		if (pos>=0)
			org =dnUser.substring(pos+marker.length());
		else
			dnUser="cn="+dnUser+",cn=organizational users,"+org;
		try {
			return connector.createSOAPMethod(dnUser, org, namespace, methodName);
		}
		catch (DirectoryException e) {  throw new RuntimeException(e); }
	}
	
	public int callMethod(CallContext context, int method) {
    	String user=DnUtil.getFirstDnPart(context.getOrganizationalUser());
		MethodCache caller = responseCache;
		Monitor mon1 = MonitorFactory.start("OutgoingCall:"+NomUtil.getUniversalName(method));
		Monitor mon2 = MonitorFactory.start("AllOutgoingCalls");
		Monitor monu1 = MonitorFactory.start("OutgoingCallForUser:"+user+":"+NomUtil.getUniversalName(method));
		Monitor monu2 = MonitorFactory.start("AllOutgoingCallsForUser:"+user);
		boolean succes=false;
    	final Date startTime=new Date();
		try {
			context.traceInfo("sending request: ", method);
			//int response = connector.sendAndWait(Node.getParent(method));
    		int response = caller.sendAndWait(Node.getParent(method),BaseSettings.timeout.get(context.getProps()));

			context.traceInfo("received response: ", response);
			succes=true;
			return response;
		}
		finally {
			logPerformance("CALL", context, startTime, method, succes);
			mon1.stop();
			mon2.stop();
			monu1.stop();
			monu2.stop();
		}

	}
	public Props getProps() {	return props;	}

	public ResourcePool getResourcePool(String poolName) {
		if (poolName==null)
			return null;
		synchronized(resourcePoolMap) {
			ResourcePool result=resourcePoolMap.get(poolName);
			if (result!=null)
				return result;
			ResourcePoolSettings settings=BaseSettings.resourcepool.get(poolName);
			ResourcePool pool=settings.create(getProps());
			resourcePoolMap.put(poolName,pool);
			return pool;
		}		
	}

	private ArrayList<PerformanceLogger> loggers = new ArrayList<PerformanceLogger>();
	public void addPerformanceLogger(PerformanceLogger log) { loggers.add(log); }
	public void logPerformance(String type, CallContext context, Date startTime, int node, boolean status) {
		for (PerformanceLogger log: loggers)
			log.log(type, context, startTime, node, status);
	}

	private ArrayList<XmlInterceptor> interceptors = new ArrayList<XmlInterceptor>();
	public void addXmlInterceptor(XmlInterceptor interceptor) { interceptors.add(interceptor); }
	public void interceptXml(ExecutionContext context, int node) {
		for (XmlInterceptor interceptor: interceptors)
			interceptor.intercept(context, node);
	}

}
