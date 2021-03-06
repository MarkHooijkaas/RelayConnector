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

package org.kisst.cordys.esb;

import org.kisst.cordys.connector.BaseConnector;
import org.kisst.cordys.util.SoapUtil;
import org.kisst.props4j.LayeredProps;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.SOAPTransaction;
import com.eibus.xml.nom.Node;

public class EsbConnector extends BaseConnector {
    public EsbConnector(){
    }
	@Override
	protected String getConnectorName() { return "EsbConnector"; }
    
    public ApplicationTransaction createTransaction(SOAPTransaction stTransaction) {
    	int env=stTransaction.getRequestEnvelope();
    	int req=SoapUtil.getContent(env);
    	String methodName=Node.getLocalName(req);
    	LayeredProps lprops=new LayeredProps(props);
    	lprops.addLayer(props.getProps("override.method."+methodName, null));
    	// TODO: special characters String namespace=Node.getNamespaceURI(req);
    	//lprops.addLayer(props.getProps("override.namespace."+namespace, null));
		return new EsbTransaction(lprops);
	}

}
