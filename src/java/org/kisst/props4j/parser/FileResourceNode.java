package org.kisst.props4j.parser;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.kisst.util.FileUtil;

public class FileResourceNode extends ResourceNode {
	private final File file;

	public FileResourceNode(String filename) { this(new File(filename)); }
	public FileResourceNode(File file) { this.file = file; }

	public String getUrlType() { return "file"; }
	public String getShortName() { return file.getName(); }
	public String getFullName() { return "file:"+file.getPath(); }
	public boolean isLeaf() { return file.isFile(); }
	public FileResourceNode getParent() { return new FileResourceNode(file.getParentFile()); }
	public Reader getReader() { 
		return new InputStreamReader(FileUtil.open(file));
	}

	public List<ResourceNode> getChildren(String extension){
		List<ResourceNode> result=new ArrayList<ResourceNode>();
		for (File f: file.listFiles()) {
			if (extension==null || f.getName().endsWith(extension))
				result.add(new FileResourceNode(f));
		}
		return result;
	}
	@Override
	public ResourceNode getChild(String path) {
		if (isDirectory())
			return new FileResourceNode(new File(file,path));
		else
			return new FileResourceNode(new File(file.getParentFile(),path));
	}
}
