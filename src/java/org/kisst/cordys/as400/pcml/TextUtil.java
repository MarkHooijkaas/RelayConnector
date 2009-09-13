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
