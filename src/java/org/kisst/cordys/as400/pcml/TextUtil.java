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

package org.kisst.cordys.as400.pcml;

import com.eibus.xml.nom.Node;

public class TextUtil {
	static private final int MAX_LENGTH=9999;
    static final protected String spaces;	
  	static {	// Anonymous block to initialise spaces at class construction.
  		StringBuilder builder = new StringBuilder();
  		while (builder.length()<MAX_LENGTH)
  			builder.append("          ");
  		spaces = builder.substring(0, MAX_LENGTH);
  	}

	public static String extractAndPad(String str, int index, int size) {
		int pos = index*size;
        int len = str.length();
        
        if (pos >= len) {
            // Trying to read after the end. Just return padding.
            return spaces.substring(0,size);
        }
        
        if (pos + size <= len) {
            // Reading at the middle of the string, no padding necessary.
            return str.substring(pos, pos + size );
        }
        // Read from pos and append enought spaces
        // Note that a Stringbuilder should not be more efficient for adding just two strings
        return str.substring(pos)+spaces.substring(0, size  - (len - pos));
    }    
	
	// TODO: These are not really text utility functions, and should be placed
	// in a more approriate package
	public static int parseCountAttribute(int node) {
		String countString = Node.getAttribute(node, "count");
		if (countString==null)
			return 1;
		int result = Integer.parseInt(countString);
		if (result<=0)
			throw new RuntimeException("count attribute is "+result+" but should be >0");
		return result; 
	}
	public static int parseSizeAttribute(int node) {
		int result = Integer.parseInt(Node.getAttribute(node, "size"));
		if (result<=0) { 
			throw new RuntimeException("size attribute not set");
		}
		return result;
	}
	public static int parseLengthAttribute(int node) {
		int result = Integer.parseInt(Node.getAttribute(node, "length"));
		if (result<=0) { 
			throw new RuntimeException("length attribute not set");
		}
		return result;
	}   

	public static int parsePrecisionAttribute(int node) {
		String str=Node.getAttribute(node, "precision");
		if (str==null)
			throw new RuntimeException("precision attribute not set");
		int result = Integer.parseInt(str);
		if (result<0) { 
			throw new RuntimeException("precision attribute should be positive");
		}
		return result;
	}   

}
