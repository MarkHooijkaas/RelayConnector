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

package org.kisst.cordys.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

import java.io.ByteArrayInputStream;

import org.kisst.cfg4j.Props;
import org.kisst.cordys.relay.CallContext;
import org.kisst.cordys.relay.RelayTrace;
import org.kisst.cordys.relay.RelayedSoapFaultException;
import org.kisst.cordys.util.NomNode;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.BodyBlock;
import com.eibus.soap.SOAPTransaction;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

public class GroovyTransaction  implements ApplicationTransaction {
	//private final static CompositeSetting groovy=new CompositeSetting(null,"groovy");
	private final static GroovyClassLoader loader = new GroovyClassLoader();

	//private final Props props;
	private final CallContext context; 
	private Class<?> scriptClass;
	
	public GroovyTransaction(GroovyConnector groovyConnector, String fullMethodName, Props props, SOAPTransaction stTransaction) {
		//this.props=props;
		context=new CallContext(groovyConnector, fullMethodName, props, stTransaction);
	}

    public boolean canProcess(String callType) {
    	if ("GroovyCall".equals(callType))
    		return true;
    	else
    		return false;
    }
    
    public void commit() {}
    public void abort() {}

    public boolean process(BodyBlock request, BodyBlock response) {
		String scriptText = Node.getData(request.getMethodDefinition().getImplementation());
		scriptClass = loader.parseClass(new ByteArrayInputStream(scriptText.toString().getBytes()), "dummySource");
    	try {
    		execute(request.getXMLNode(), response.getXMLNode());
    	}
    	catch (RelayedSoapFaultException e) {
    		e.createResponse(response);
    	}
    	catch (Exception e) {
    		RelayTrace.logger.log(Severity.ERROR, "TECHERR.GROOVY", e);
    		response.createSOAPFault("TECHERR.GROOVY",e.toString());
    	}
        return true; // connector has to send the response
    }
	
	

	private void execute(int response, int request) throws Exception{
		// Does each new invocation need a new instance?
		// probably, considering the bindings
		if (context.debugTraceEnabled())
			context.traceDebug("executing Groovy script class "+scriptClass.getName());
		Script script = (Script) scriptClass.newInstance();
		script.getBinding().setVariable("context", context);
		script.getBinding().setVariable("input",  new NomNode(request));
		script.getBinding().setVariable("output", new NomNode(response));
		script.run();
	}	
}
