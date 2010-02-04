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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.kisst.cfg4j.MultiLevelProps;
import org.kisst.cfg4j.Props;
import org.kisst.cordys.relay.Module;
import org.kisst.cordys.relay.RelaySettings;
import org.kisst.cordys.relay.resourcepool.ResourcePool;
import org.kisst.cordys.relay.resourcepool.ResourcePoolSettings;

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

abstract public class BaseConnector extends ApplicationConnector {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(BaseConnector.class);

	abstract protected String getConnectorName();
	abstract public ApplicationTransaction createTransaction(SOAPTransaction stTransaction);

	private HashMap<String,ResourcePool> resourcePoolMap=new HashMap<String,ResourcePool>();

	private Connector connector;
	private String configLocation;
	private String dnOrganization;
	private ArrayList<Module> modules=new ArrayList<Module>();
	protected MultiLevelProps mlprops;

	/**
	 * This method gets called when the processor is started. It reads the
	 * configuration of the processor and creates the connector with the proper
	 * parameters.
	 * It will also create a client connection to Cordys.
	 *
	 * @param processor The processor that is started.
	 */
	public void open(Processor processor)
	{
		dnOrganization=processor.getOrganization();
		try {
			initConfigLocation(getConfiguration());
			connector= Connector.getInstance(getConnectorName());
			if (!connector.isOpen())
				connector.open();

			mlprops=new MultiLevelProps(getConfigStream());
			init(getGlobalProps());
			addDynamicModules(getGlobalProps());
			for (int i=0; i<modules.size(); i++)
				modules.get(i).init(getGlobalProps());
		}
		catch (DirectoryException e) { throw new RuntimeException(e);	}
		catch (ExceptionGroup e) { throw new RuntimeException(e);	} 
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

	@Override
	public void reset(Processor processor) { reset(); }


	public void reset() {
		mlprops =new MultiLevelProps(getConfigStream());
		for (int i=0; i<modules.size(); i++)
			modules.get(i).reset(getGlobalProps());
	}

	public void close(Processor processor)
	{
		for (int i=0; i<modules.size(); i++)
			modules.get(i).destroy();
	}    


	protected IManagedComponent createManagedComponent() {
		IManagedComponent mc=super.createManagedComponent();
		return mc;
	}




	@Override protected String getManagedComponentType() { return getConnectorName(); }
	@Override protected String getManagementName() { return getConnectorName(); }

	public Connector getConnector() { return connector; }
	public String getOrganization() { return dnOrganization; }

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
			response=callMethod(method);
			int node=SoapUtil.getContent(response);
			String config = Node.getData(node);
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
		String org=dnOrganization;
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

	public int callMethod(int method) {
		try {
			//logger.log(Severity.INFO, "sending request\n"+Node.writeToString(method, true));
			return connector.sendAndWait(Node.getParent(method));
		}
		catch (TimeoutException e) { throw new RuntimeException(e); }
		catch (ExceptionGroup e) { throw new RuntimeException(e); }
	}
	public Props getGlobalProps() {	return mlprops.getGlobalProps();	}

	public ResourcePool getResourcePool(String poolName) {
		if (poolName==null)
			return null;
		synchronized(resourcePoolMap) {
			ResourcePool result=resourcePoolMap.get(poolName);
			if (result!=null)
				return result;
			ResourcePoolSettings settings=RelaySettings.resourcepool.get(poolName);
			ResourcePool pool=settings.create(getGlobalProps());
			resourcePoolMap.put(poolName,pool);
			return pool;
		}		
	}
}
