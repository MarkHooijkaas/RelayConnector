package org.kisst.cordys.connector;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.kisst.props4j.parser.ResourceNode;

public class XmlStoreResourceNode extends ResourceNode {
	private static BaseConnector connector;
	static void initConnector(BaseConnector connector) {XmlStoreResourceNode.connector = connector; }
	
	private final String key;

	public XmlStoreResourceNode(String key) {
		if (key.endsWith("/"))
			key=key.substring(key.length()-1);
		if (key.toLowerCase().startsWith("xmlstore:"))
			this.key=key.substring(9);
		else
			this.key=key;
	}

	public String getUrlType() { return "xmlstore"; }
	public String getShortName() {
		int pos=key.lastIndexOf('/');
		if (pos>0)
			return key.substring(pos+1);
		else
			return key;
	}
	public String getFullName() { return key;}
	public boolean isLeaf() { return true; } // TODO
	public XmlStoreResourceNode getParent() {
		int pos=key.lastIndexOf('/');
		if (pos>0)
			return new XmlStoreResourceNode(key.substring(0,pos-1));
		else
			throw new RuntimeException("XmlStore key "+key+" has no parent");
		
	}
	public Reader getReader() { 
		String parts[]= key.split("[@]");
		if (parts.length !=2)
			throw new RuntimeException("Correct xmlstore configLocation format is xmlstore:dnUser@key");
		String dnUser=parts[0];
		String key=parts[1];
		InputStream stream = connector.getDataFromXmlStore(key, dnUser);
		return new InputStreamReader(stream);
	}

	public List<ResourceNode> getChildren(String extension){
		List<ResourceNode> result=new ArrayList<ResourceNode>();
		// TODO:
		return result;
	}
	@Override
	public ResourceNode getChild(String path) {
		if (isDirectory())
			return new XmlStoreResourceNode(key+"/"+path);
		int pos=key.lastIndexOf('/');
		if (pos>0)
			return new XmlStoreResourceNode(key.substring(0,pos)+"/"+path);
		else
			return new XmlStoreResourceNode(path);
	}


}
