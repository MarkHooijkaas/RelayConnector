package org.kisst.props4j.parser;

import java.io.Reader;
import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.List;

import org.kisst.cordys.util.ReflectionUtil;

public abstract class ResourceNode {
	private final static LinkedHashMap<String, Constructor<? extends ResourceNode>> urltypes=new LinkedHashMap<String, Constructor<? extends ResourceNode>>();
	
	abstract public String getUrlType();
	abstract public String getShortName();
	abstract public String getFullName();
	abstract public boolean isLeaf();
	public boolean isDirectory() { return ! isLeaf(); }
	abstract public Reader getReader();
	abstract public ResourceNode getParent();
	abstract protected ResourceNode getChild(String path);
	abstract public List<ResourceNode> getChildren(String extension);

	
	@SuppressWarnings("unchecked")
	public static void registerType(String key, Class<? extends ResourceNode> cls) {
		Constructor<?> cons = ReflectionUtil.getConstructor(cls, new Class[]{String.class});
		urltypes.put(key, (Constructor<? extends ResourceNode>) cons);
	}
	
	public static ResourceNode create(String path) {
		String lowerpath = path.toLowerCase();
		for (String key: urltypes.keySet()) {
			if (lowerpath.startsWith(key)) {
				Constructor<? extends ResourceNode> cons=urltypes.get(key);
				return (ResourceNode) ReflectionUtil.createObject(cons, new Object[]{path});
			}
		}
		return null;
	}
	
	public ResourceNode getPath(String path) {
		ResourceNode result = create(path);
		if (result==null)
			result=getChild(path);
		return result;
	}
}
