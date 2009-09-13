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

package org.kisst.cordys.script;

import org.kisst.cordys.relay.SoapFaultException;



/** 
 * This Exception class is used to indicate an error in a Script compilation.
 * 
 */
public class CompilationException extends SoapFaultException {
	private static final long serialVersionUID = 1L;

	//private final CompilationContext compiler;

	public CompilationException(CompilationContext compiler, String faultstring, Throwable e) {
		super("Method.Compilation.Error",faultstring, e);
		//this.compiler=compiler;
	}

	public CompilationException(CompilationContext compiler, String faultstring) {
		super("Method.Compilation.Error",faultstring);
		//this.compiler=compiler;
	}
}	

