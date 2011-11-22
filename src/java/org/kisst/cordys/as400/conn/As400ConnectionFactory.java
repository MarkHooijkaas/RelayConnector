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

package org.kisst.cordys.as400.conn;

import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.kisst.cordys.as400.As400PoolSettings;
import org.kisst.props4j.Props;

public class As400ConnectionFactory implements KeyedPoolableObjectFactory {

	private final As400PoolSettings settings;
	private final Props props;
	
	public As400ConnectionFactory(As400PoolSettings settings, Props props) {
		this.settings=settings;
		this.props=props;	}
	
	public Object makeObject(Object key) {
		return new As400Connection(settings, props, (String) key);
	}

	public void activateObject(Object obj, Object key) {
	}

	public void passivateObject(Object obj, Object key) {
	}

	public void destroyObject(Object obj, Object key) {
		As400Connection conn= (As400Connection) obj;
		conn.close();
	}


	public boolean validateObject(Object obj, Object key) {
		return true;
	}

}
