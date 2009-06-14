package org.kisst.cordys.relay;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.kisst.cfg4j.MultiLevelProps;
import org.kisst.cfg4j.Props;
import org.kisst.cordys.script.Script;
import org.kisst.cordys.util.NomUtil;
import org.kisst.cordys.util.SoapUtil;

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

public class RelayConnector extends ApplicationConnector {
	private static final CordysLogger logger = CordysLogger.getCordysLogger(RelayConnector.class);
    public  static final String CONNECTOR_NAME = "RelayConnector";
    
	//public final RelayConfiguration conf=new RelayConfiguration();
	private Connector connector;
	private String configLocation;
	private String dnOrganization;
	final HashMap<String, Script> scriptCache=new HashMap<String, Script>();
	//public Properties properties=null;
	private ArrayList<Module> modules=new ArrayList<Module>();
	public final MethodCache responseCache=new MethodCache();
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
    	addModule(new RelayModule());
        dnOrganization=processor.getOrganization();
        try {
    		initConfigLocation(getConfiguration());
            connector= Connector.getInstance(CONNECTOR_NAME);
            if (!connector.isOpen())
                connector.open();

            mlprops=new MultiLevelProps(getConfigStream());
            responseCache.init(connector, getGlobalProps());
            addDynamicModules(getGlobalProps());
        	for (int i=0; i<modules.size(); i++)
        		modules.get(i).init(getGlobalProps());
        }
        catch (DirectoryException e) { throw new RuntimeException(e);	}
        catch (ExceptionGroup e) { throw new RuntimeException(e);	} 
    }

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

    public ApplicationTransaction createTransaction(SOAPTransaction stTransaction) {
    	int env=stTransaction.getRequestEnvelope();
    	int req=SoapUtil.getContent(env);
    	String key=NomUtil.getUniversalName(req);
		return new RelayTransaction(this, key, mlprops.getProps(key));
	}

    @Override
	public void reset(Processor processor) { reset(); }


	public void reset() {
	    mlprops =new MultiLevelProps(getConfigStream());
        responseCache.reset(getGlobalProps());
		scriptCache.clear();
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
    
    protected String getManagedComponentType() { return "RelayConnector"; }
    protected String getManagementName() { return "RelayConnector"; }

	public Connector getConnector() { return connector; }
	public String getOrganization() { return dnOrganization; }

	private void initConfigLocation(int configNode) 
	{
		logger.debug("RelayConfiguration starting initialisation");
		if (configNode == 0)
			throw new RuntimeException("Configuration not found.");

		if (!Node.getLocalName(configNode).equals("configuration"))
			throw new RuntimeException("Root-tag of the configuration should be <configuration>");
		configNode=NomUtil.getElementByLocalName(configNode, "Configuration");
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
}
