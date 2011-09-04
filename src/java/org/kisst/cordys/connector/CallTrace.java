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

package org.kisst.cordys.connector;

import java.util.ArrayList;

import org.kisst.cordys.util.NomUtil;
import org.kisst.props4j.Props;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

/**
 * This class supports tracing all details of method calls, for debugging purposes.
 * This could not be done using log4j only, because it is meant that tracing can be turned on and off for 
 * individual methods. 
 * Furthermore, the trace should be appended to any response or SOAP:Fault that the method returns.
 */
public class CallTrace {
	public static final CordysLogger logger = CordysLogger.getCordysLogger(CallTrace.class);

	public static class Item {
		final String msg;
		final int node;
		Item(String msg) { this.msg=msg; this.node=0; }
		public Item(String msg, int node) { this.msg=msg; this.node=node; }
	}
	private final ArrayList<Item> items=new ArrayList<Item>();
	private final Severity traceLevel;

	public CallTrace(Severity traceLevel) {
		this.traceLevel=traceLevel;
	}
	
	public void traceDebug(String msg) { 
		if (debugTraceEnabled())
			trace(Severity.DEBUG,msg);
	} 
	public void traceInfo(String msg) {	trace(Severity.INFO,msg); }
	public void traceInfo(String msg, int node)  { trace(Severity.INFO, new CallTrace.Item(msg,node));	}

	public synchronized void trace(Severity level, String msg, int node) { trace(level, new Item(msg, node)); }
	public synchronized void trace(Severity level, String msg) { trace(level, new Item(msg)); }
	public synchronized void trace(Severity level, Item item) {
		if (item.node==0)
			logger.log(level,item.msg);
		else
			logger.log(level,item.msg+Node.writeToString(item.node, false));
		if (! infoTraceEnabled()) 
			// trace should be at least on info level, to be added to the trace buffer
			// otherwise an ERROR would fill the trace 
			return;
		items.add(item);
	} 

	public boolean debugTraceEnabled() { return (traceLevel!=null && Severity.DEBUG.isGreaterOrEqual(traceLevel)) || logger.isDebugEnabled(); }
	public boolean infoTraceEnabled()  { return (traceLevel!=null && Severity.INFO. isGreaterOrEqual(traceLevel)) || logger.isInfoEnabled(); }
	public String getTraceAsString(Props props) {
		boolean showEnvelope=BaseSettings.traceShowEnvelope.get(props);
		StringBuffer buf=new StringBuffer();
		for (Item i:items) {
			buf.append(i.msg);
			if (i.node!=0) {
				if (showEnvelope)
					buf.append(Node.writeToString(NomUtil.getRootNode(i.node), false));
				else
					buf.append(Node.writeToString(i.node, false));
			}
			buf.append('\n');
		}
		return buf.toString();
	}

	public void addToNode(int node, Props props) {
		boolean showEnvelope=BaseSettings.traceShowEnvelope.get(props);
		for (Item i:items) {
			int itemnode = Node.createTextElement("item", i.msg, node);
			if (i.node!=0) {
				int srcnode=i.node;
				if (showEnvelope)
					srcnode=NomUtil.getRootNode(i.node);
				Node.duplicateAndAppendToChildren(srcnode, srcnode, itemnode);
			}
		}
	}
}
