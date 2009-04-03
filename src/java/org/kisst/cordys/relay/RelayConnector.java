package org.kisst.cordys.relay;

import java.util.HashMap;
import java.util.Properties;

import org.kisst.cordys.script.Script;

import com.eibus.connector.nom.Connector;
import com.eibus.directory.soap.DirectoryException;
import com.eibus.exception.ExceptionGroup;
import com.eibus.management.IManagedComponent;
import com.eibus.soap.ApplicationConnector;
import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.MethodDefinition;
import com.eibus.soap.Processor;
import com.eibus.soap.SOAPTransaction;

public class RelayConnector extends ApplicationConnector {
	//private static final CordysLogger logger = CordysLogger.getCordysLogger(RelayConnector.class);
    public  static final String CONNECTOR_NAME = "RelayConnector";

	public final RelayConfiguration conf=new RelayConfiguration(null);
	private Connector connector;
	private String dnOrganization;
	private final HashMap<String, Script> scriptCache=new HashMap<String, Script>();
	public Properties properties=null;
	private Module[] modules=new Module[0];
	
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
    		conf.init(getConfiguration());
            connector= Connector.getInstance(CONNECTOR_NAME);

            String moduleList=conf.get("modules");
            if (moduleList!=null && moduleList.trim().length()>0) {
            	String[] moduleNames=moduleList.split(",");
            	modules=new Module[moduleNames.length];
            	for (int i=0; i<modules.length; i++) {
            		try {
						modules[i]=(Module) Class.forName(moduleNames[i].trim()).newInstance();
					} catch (Exception e) {
						throw new RuntimeException("Could not load module class "+moduleNames[i]);
					}
            	}
            }
        	for (int i=0; i<modules.length; i++)
        		modules[i].init(this);
        	            
            if (!connector.isOpen())
            {
                connector.open();
            }
        }
        catch (DirectoryException e) { throw new RuntimeException(e);	}
        catch (ExceptionGroup e) { throw new RuntimeException(e);	} 
    }

	public ApplicationTransaction createTransaction(SOAPTransaction stTransaction) {
		return new RelayTransaction(this);
	}

	@Override
	public void reset(Processor processor) {
		reset();
	}

	public void reset() {
		conf.load();
		scriptCache.clear();
    	for (int i=0; i<modules.length; i++)
    		modules[i].reset();
	}

	public void close(Processor processor)
    {
		for (int i=0; i<modules.length; i++)
			modules[i].destroy();
    }    


    protected IManagedComponent createManagedComponent() {
    	IManagedComponent mc=super.createManagedComponent();
    	return mc;
    }
    
    protected String getManagedComponentType() { return "RelayConnector"; }
    protected String getManagementName() { return "RelayConnector"; }

	public Connector getConnector() { return connector; }
	public String getOrganization() { return dnOrganization; }
	
	public Script getScript(MethodDefinition def) {
		String methodName=def.getNamespace()+"/"+def.getMethodName();
		Script script=scriptCache.get(methodName);
		if (script==null) {
			script=new Script(this, def);
			if (conf.getCacheScripts())
				scriptCache.put(methodName, script);
		}
		return script;
	}
}
