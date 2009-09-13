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



/** 
 * This Exception class is used to have more control of generated SOAP Fault messages.
 * 
 * When an error somewhere in the code happens, one could normally throw any kind of Exception.
 * This would be caught by the Cordys Framework and translated into a SOAP:Fault with 
 * the Exception Message and stack trace
 * If one would use this class or any of it's children classes instead, one has total control 
 * over the SOAP:Fault message created.
 * Also no stack trace is added to the details section. 
 * This is considered good practice, since Stack traces might show vulnerable information. 
 *
 */
public class SoapFaultException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final String faultcode;
	private final String faultstring;

	public SoapFaultException(String faultcode, String faultstring) {
		super(faultstring);
		this.faultcode=faultcode;
		this.faultstring=faultstring;
	}
	
	public SoapFaultException(String faultcode, String faultstring, Throwable e) {
		super(faultstring, e);
		this.faultcode=faultcode;
		this.faultstring=faultstring;
	}

	public String getFaultcode()   { return faultcode; } 
	public String getFaultstring() { return faultstring; }	

	protected boolean hasDetails()     { return false; }
	protected void    fillDetails(int node)    { }

}	

