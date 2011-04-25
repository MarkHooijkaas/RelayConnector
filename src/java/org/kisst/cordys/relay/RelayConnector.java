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

package org.kisst.cordys.relay;

import org.kisst.cfg4j.LayeredProps;
import org.kisst.cfg4j.Props;
import org.kisst.cordys.util.BaseConnector;
import org.kisst.cordys.util.JamonUtil;
import org.kisst.cordys.util.NomUtil;
import org.kisst.cordys.util.SoapUtil;

import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.SOAPTransaction;
import com.eibus.xml.nom.Node;

public class RelayConnector extends BaseConnector {
	public final MethodCache responseCache=new MethodCache();

	public RelayConnector() {
		addModule(new RelayModule());
	}
	
	@Override protected void init(Props globalProps) {
		responseCache.init(getConnector(), getGlobalProps());
	}

	@Override protected String getConnectorName() { return "RelayConnector"; }


    public ApplicationTransaction createTransaction(SOAPTransaction stTransaction) {
    	int env=stTransaction.getRequestEnvelope();
    	int req=SoapUtil.getContent(env);
    	String fullMethodName=NomUtil.getUniversalName(req);
    	LayeredProps props=new LayeredProps(mlprops.getGlobalProps());
    	props.addLayer(mlprops.getProps("method:"+fullMethodName));
    	props.addLayer(mlprops.getProps("namespace:"+Node.getNamespaceURI(req)));
		return new RelayTransaction(this, fullMethodName, props, stTransaction);
	}

	@Override public void reset() {
		super.reset();
		responseCache.reset(getGlobalProps());
		JamonUtil.logAndResetAllTimers("d:/Cordys/relay.jamon.log");
	}
}
