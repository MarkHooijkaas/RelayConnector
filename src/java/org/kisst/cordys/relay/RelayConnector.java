package org.kisst.cordys.relay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.kisst.cordys.script.TopScript;
import org.kisst.cordys.util.NomUtil;

import com.eibus.connector.nom.Connector;
import com.eibus.directory.soap.DirectoryException;
import com.eibus.exception.ExceptionGroup;
import com.eibus.management.IManagedComponent;
import com.eibus.soap.ApplicationConnector;
import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.Processor;
import com.eibus.soap.SOAPTransaction;
import com.eibus.xml.nom.Node;

public class RelayConnector extends ApplicationConnector {
	//private static final CordysLogger logger = CordysLogger.getCordysLogger(RelayConnector.class);
    public  static final String CONNECTOR_NAME = "RelayConnector";

	//public final RelayConfiguration conf=new RelayConfiguration();
	private Connector connector;
	private String configLocation;
	private String dnOrganization;
	final HashMap<String, TopScript> scriptCache=new HashMap<String, TopScript>();
	//public Properties properties=null;
	private ArrayList<Module> modules=new ArrayList<Module>();
	public final MethodCache responseCache=new MethodCache();
	
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
    		init(getConfiguration());
            connector= Connector.getInstance(CONNECTOR_NAME);
            Properties properties=RelayConfiguration.load(configLocation);
            responseCache.init(connector, properties);
            addDynamicModules(properties);
        	for (int i=0; i<modules.size(); i++)
        		modules.get(i).init(properties);
        	            
            if (!connector.isOpen())
            {
                connector.open();
            }
        }
        catch (DirectoryException e) { throw new RuntimeException(e);	}
        catch (ExceptionGroup e) { throw new RuntimeException(e);	} 
    }

	private void addDynamicModules(Properties properties) {
		String moduleList=(String) properties.get("modules");
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
		return new RelayTransaction(this);
	}

	@Override
	public void reset(Processor processor) {
        Properties properties=RelayConfiguration.load(configLocation);
		reset(properties);
	}

	public void reset(Properties properties) {
        responseCache.reset(properties);
		scriptCache.clear();
    	for (int i=0; i<modules.size(); i++)
    		modules.get(i).reset(properties);
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

	public void init(int configNode) 
	{
		//logger.debug("RelayConfiguration starting initialisation");
		if (configNode == 0)
			throw new RuntimeException("Configuration not found.");

		if (!Node.getLocalName(configNode).equals("configuration"))
			throw new RuntimeException("Root-tag of the configuration should be <configuration>");
		configNode=NomUtil.getElementByLocalName(configNode, "Configuration");
		configLocation = Node.getData(NomUtil.getElementByLocalName(configNode,"ConfigLocation")); 
	}

}
