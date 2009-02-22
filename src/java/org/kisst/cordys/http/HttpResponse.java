package org.kisst.cordys.http;

import java.io.UnsupportedEncodingException;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.XMLException;

public class HttpResponse {
	private final int code;
	private final byte[] response;
	
	public HttpResponse(final int code, final byte[] response) {
		this.code = code;
		this.response=response;
	}
	
	public int getCode() { return code; }
	public String getResponseString() { 
		try {
			return new String(response, "UTF-8");
		} catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
	}
	/**
	 * Returns a NOM node with the parsed response or 0 if it is not valid XML.
	 * 
	 * Note, the caller of this method is responsible for deleting this NOM node.
	 * 
	 * @param doc the document to load the XML document in
	 * @return the NOM node or 0 if the http response was not valid XML
	 */
	public int getResponseXml(Document doc) { 
		try {
			return doc.load(response);
		} 
		catch (XMLException e) { 
			return 0;
		}
	}
}
