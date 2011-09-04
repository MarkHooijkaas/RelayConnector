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

package org.kisst.cordys.http;

import org.kisst.cordys.connector.BaseConnector;
import org.kisst.cordys.connector.Module;
import org.kisst.cordys.script.GenericCommand;
import org.kisst.cordys.script.commands.CommandList;

public class HttpModule implements Module {
	private BaseConnector connector;

	public String getName() { return "HttpModule";	}

	public void init(BaseConnector connector) {
		this.connector = connector;
    	CommandList.addBasicCommand("http",  new GenericCommand(HttpStep.class));
    	CommandList.addBasicCommand("http-relay",  new GenericCommand(HttpRelayStep.class));
    	CommandList.addBasicCommand("http-callback",  new GenericCommand(HttpCallbackStep.class));
    	reset();
	}

	public void reset() {
		HttpBase.reset(connector.getGlobalProps());
    	//settings=new MultiLevelSettings<HttpSettings>(mlprops,"http", HttpSettings.class);
	}

	public void destroy() {}
}
