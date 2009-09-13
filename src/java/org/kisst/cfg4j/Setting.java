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

package org.kisst.cfg4j;


public class Setting {
  protected final CompositeSetting parent;
  protected final String fullName;
  protected final String name;
  //private final boolean required;

  public Setting(CompositeSetting parent, String name) {
	this.parent=parent;
	this.name=name;
	//this.required=required;
	if(parent==null) 
	  fullName=name;
	else {
		parent.add(this);
		if (parent.fullName==null)
			fullName=name;
		else
			fullName=parent.fullName+"."+name;
	}
  }
  
  //public boolean ok() { return ! this.required; }


  //abstract public String asString();
}
