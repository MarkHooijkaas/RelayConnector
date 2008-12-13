package org.kisst.cordys.relay;

import java.util.HashMap;

import org.kisst.cordys.script.Script;

import com.cordys.coe.coelib.LibraryVersion;
import com.cordys.coe.exception.GeneralException;
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
        // Check the CoELib version.
        try
        {
            LibraryVersion.loadAndCheckLibraryVersionFromResource(this.getClass(), true);
        }
        catch (GeneralException e) { throw new RuntimeException(e); } 
        
        dnOrganization=processor.getOrganization();
        try {
    		conf.init(getConfiguration());
            connector= Connector.getInstance(CONNECTOR_NAME);
            // The connection pool must be initialized, before connector.open is called,
            // because as soon as open is called, messages can be received 
            // and the pool must be ready.
            if (!connector.isOpen())
            {
                connector.open();
            }
        	//logger.debug("RelayConnector opened");
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
	}

	public void close(Processor processor)
    {
    	//logger.log(Severity.INFO, "RelayConnector closed");
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
